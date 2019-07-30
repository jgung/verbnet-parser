/*
 * Copyright 2019 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.semlink.app;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.example.SequenceExample;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.semlink.extractor.SequenceExampleExtractor;
import io.github.semlink.extractor.Vocabulary;
import io.github.semlink.extractor.config.ConfigSpec;
import io.github.semlink.extractor.config.Extractors;
import io.github.semlink.tensor.TensorList;
import io.github.semlink.type.Fields;
import io.github.semlink.type.HasFields;
import io.github.semlink.type.IToken;
import io.github.semlink.type.ITokenSequence;
import io.github.semlink.type.Token;
import io.github.semlink.type.TokenSequence;
import lombok.NonNull;
import lombok.Setter;

import static io.github.semlink.tensor.Tensors.batchExamples;
import static io.github.semlink.tensor.Tensors.toArcProbs;
import static io.github.semlink.tensor.Tensors.toRelProbs;
import static io.github.semlink.tensor.Tensors.toStringLists;

/**
 * Tensorflow-based dependency parser.
 *
 * @author jgung
 */
public class DependencyParser implements AutoCloseable {

    private static final String OP_NAME = "input_example_tensor";

    private SequenceExampleExtractor featureExtractor;
    private SavedModelBundle model;

    @Setter
    private String posTensor = "pos/labels";
    @Setter
    private String arcTensor = "deprel/Softmax";
    @Setter
    private String relTensor = "deprel/transpose_1";

    private Vocabulary relVocabulary;

    public DependencyParser(@NonNull SequenceExampleExtractor featureExtractor,
                            @NonNull SavedModelBundle model) {
        this.featureExtractor = featureExtractor;
        this.model = model;
        this.relVocabulary = featureExtractor.vocabulary("deprel")
                .orElseThrow(IllegalArgumentException::new);
    }

    public List<ITokenSequence> predictBatch(@NonNull List<HasFields> inputs) {

        List<SequenceExample> sequenceExamples = inputs.stream()
                .map(featureExtractor::extractSequence)
                .collect(Collectors.toList());

        try (Tensor<?> inputTensor = Tensor.create(batchExamples(sequenceExamples), String.class)) {
            Session.Runner runner = model.session().runner()
                    .feed(OP_NAME, inputTensor)
                    .fetch(posTensor)
                    .fetch(arcTensor)
                    .fetch(relTensor);

            TensorList results;

            synchronized (this) {
                results = TensorList.of(runner.run());
            }

            List<List<String>> posResults = toStringLists(results.get(0));
            List<float[][]> arcProbs = toArcProbs(results.get(1));
            List<float[][][]> relProbs = toRelProbs(results.get(2));

            List<ITokenSequence> output = new ArrayList<>();

            for (int i = 0; i < inputs.size(); ++i) {
                List<IToken> tokens = new ArrayList<>();

                float[][] arcs = arcProbs.get(i);
                float[][][] rels = relProbs.get(i);
                List<String> tags = posResults.get(i);
                List<Integer> edges = nonprojective(arcs);
                List<String> labels = getLabels(rels, edges);
                List<String> words = inputs.get(i).field("word");
                for (int idx = 1; idx < edges.size(); ++idx) {
                    String tag = tags.get(idx - 1);
                    int head = edges.get(idx);
                    String label = labels.get(idx);
                    String word = words.get(idx - 1);
                    Token token = (Token) new Token(word, idx - 1)
                            .add("pos", tag)
                            .add("dep", label)
                            .add("head", head);
                    tokens.add(token);
                }

                output.add(new TokenSequence(tokens));
            }

            results.close();

            return output;
        }
    }

    private List<String> getLabels(float[][][] rels, List<Integer> edges) {
        List<String> labels = new ArrayList<>();
        for (int t = 0; t < edges.size(); ++t) {
            int edgeIndex = edges.get(t);
            int argmax = 0;
            float max = -Float.MAX_VALUE;
            int index = 0;
            for (float[] probs : rels[t]) {
                float val = probs[edgeIndex];
                if (val > max) {
                    argmax = index;
                    max = val;
                }
                ++index;
            }
            labels.add(relVocabulary.indexToFeat(argmax));
        }
        return labels;
    }

    private static List<Integer> nonprojective(float[][] probs) {
        // tokens should not point to themselves, set their scores to 0
        int index = 0;
        for (float[] edgeProbs : probs) {
            edgeProbs[index++] = 0;
        }

        // ensure sentinel is assigned to itself as a head
        for (int i = 1; i < probs[0].length; ++i) {
            probs[0][i] = 0;
        }
        probs[0][0] = 1;

        // re-normalize to probabilities
        for (float[] headProbs : probs) {
            float sum = 0;
            for (float val : headProbs) {
                sum += val;
            }
            for (int i = 0; i < headProbs.length; ++i) {
                headProbs[i] /= sum;
            }
        }

        List<Integer> edges = greedy(probs);
        List<Integer> roots = findRoots(edges);
        List<Integer> bestEdges = edges;
        float bestScore = -Float.MAX_VALUE;
        if (roots.size() > 1) {
            for (int root : roots) {
                float[][] candidateProbs = makeRoot(probs, root);
                List<Integer> candidateEdges = greedy(candidateProbs);
                float score = scoreEdges(candidateProbs, candidateEdges);
                if (score > bestScore) {
                    bestEdges = candidateEdges;
                    bestScore = score;
                }
            }
        }
        return bestEdges;
    }

    private static float scoreEdges(float[][] probs, List<Integer> edges) {
        float sum = 0;
        for (int v = 1; v < probs.length; ++v) {
            sum += Math.log(probs[v][edges.get(v)]);
        }
        return sum;
    }

    private static float[][] makeRoot(float[][] probs, int root) {
        float[][] result = new float[probs.length][];
        int index = 0;
        for (float[] val : probs) {
            if (index == root) {
                result[index] = new float[val.length];
                result[index][0] = 1;
            } else {
                result[index] = Arrays.copyOf(val, val.length);
            }

            if (index != 0 && index != root) {
                result[index][0] = 0;
            }

            normalizeInPlace(result[index]);

            ++index;
        }
        return result;
    }

    private static List<Integer> findRoots(List<Integer> edges) {
        return IntStream.range(0, edges.size())
                .filter(i -> i > 0 && edges.get(i) == 0)
                .boxed()
                .collect(Collectors.toList());
    }

    private static List<Integer> greedy(float[][] probs) {

        // argmax
        int[] edges = new int[probs.length];
        int tokenIndex = 0;
        for (float[] headProbs : probs) {
            edges[tokenIndex++] = argmax(headProbs);
        }

        List<List<Integer>> cycles;
        do {
            // try to fix cycles
            cycles = findCycles(edges);
            for (List<Integer> cycleVertices : cycles) {
                // get the best heads and their probabilities
                List<Integer> cycleEdges = cycleVertices.stream()
                        .map(v -> edges[v])
                        .collect(Collectors.toList());

                List<Float> cycleProbs = new ArrayList<>();
                for (int i = 0; i < cycleVertices.size(); ++i) {
                    cycleProbs.add(probs[cycleVertices.get(i)][cycleEdges.get(i)]);
                }
                // get the second-best edges and their probabilities
                for (int i = 0; i < cycleVertices.size(); ++i) {
                    probs[cycleVertices.get(i)][cycleEdges.get(i)] = 0;
                }
                List<Integer> backoffEdges = new ArrayList<>();
                for (int v : cycleVertices) {
                    backoffEdges.add(argmax(probs[v]));
                }
                List<Float> backoffProbs = new ArrayList<>();
                for (int i = 0; i < cycleVertices.size(); ++i) {
                    backoffProbs.add(probs[cycleVertices.get(i)][backoffEdges.get(i)]);
                }
                for (int i = 0; i < cycleVertices.size(); ++i) {
                    probs[cycleVertices.get(i)][cycleEdges.get(i)] = cycleProbs.get(i);
                }

                // Find the node in the cycle that the model is the least confident about and its probability
                int index = 0;
                float max = -Float.MAX_VALUE;
                int newRootInCycle = 0;
                for (float backoffProb : backoffProbs) {
                    float val = backoffProb / cycleProbs.get(index);
                    if (val > max) {
                        newRootInCycle = index;
                        max = val;
                    }
                    ++index;
                }
                int newCycleRoot = cycleVertices.get(newRootInCycle);

                // set the new root
                probs[newCycleRoot][cycleEdges.get(newRootInCycle)] = 0;
                edges[newCycleRoot] = backoffEdges.get(newRootInCycle);
            }
        } while (!cycles.isEmpty());

        return IntStream.of(edges).boxed().collect(Collectors.toList());
    }

    private static void normalizeInPlace(float[] values) {
        float sum = 0;
        for (float val : values) {
            sum += val;
        }
        for (int i = 0; i < values.length; ++i) {
            values[i] /= sum;
        }
    }

    private static int argmax(float[] values) {
        float max = -Float.MAX_VALUE;
        int argmax = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > max) {
                max = values[i];
                argmax = i;
            }
        }
        return argmax;
    }

    private static class CycleFinder {

        int[] edges;
        List<Integer> vertices;
        List<Integer> indices;
        List<Integer> lowlinks;
        Stack<Integer> stack;
        List<Boolean> onstack;
        int currentIndex;
        List<List<Integer>> cycles;

        CycleFinder(int[] edges) {
            this.edges = edges;
            vertices = IntStream.range(0, edges.length).boxed().collect(Collectors.toList());
            indices = new ArrayList<>(Collections.nCopies(edges.length, -1));
            lowlinks = new ArrayList<>(Collections.nCopies(edges.length, -1));
            stack = new Stack<>();
            onstack = new ArrayList<>(Collections.nCopies(edges.length, false));
            currentIndex = 0;
            cycles = new ArrayList<>();
        }

        private int strongConnect(int vertex, int currentIndex) {
            indices.set(vertex, currentIndex);
            lowlinks.set(vertex, currentIndex);
            stack.push(vertex);
            currentIndex += 1;
            onstack.set(vertex, true);

            for (int vert : IntStream.range(0, edges.length)
                    .filter(i -> edges[i] == vertex).toArray()) {
                if (indices.get(vert) == -1) {
                    currentIndex = strongConnect(vert, currentIndex);
                    lowlinks.set(vertex, Math.min(lowlinks.get(vertex), lowlinks.get(vert)));
                } else if (onstack.get(vert)) {
                    lowlinks.set(vertex, Math.min(lowlinks.get(vertex), indices.get(vert)));
                }
            }

            if (lowlinks.get(vertex).equals(indices.get(vertex))) {
                List<Integer> cycle = new ArrayList<>();
                int vert = -1;
                while (vert != vertex) {
                    vert = stack.pop();
                    onstack.set(vert, false);
                    cycle.add(vert);
                }
                if (cycle.size() > 1) {
                    cycles.add(cycle);
                }

            }
            return currentIndex;
        }

        private List<List<Integer>> findCycles() {
            for (int vertex : vertices) {
                if (indices.get(vertex) == -1) {
                    currentIndex = strongConnect(vertex, currentIndex);
                }
            }
            return cycles;
        }

    }

    private static List<List<Integer>> findCycles(int[] edges) {
        return new CycleFinder(edges).findCycles();
    }

    public static DependencyParser fromDirectory(@NonNull String modelDir) {
        try (FileInputStream in = new FileInputStream(Paths.get(modelDir, "config.json").toString())) {
            ConfigSpec spec = ConfigSpec.fromInputStream(in);
            SequenceExampleExtractor extractor = Extractors.createExtractor(spec.features(),
                    Paths.get(modelDir, "vocab").toString(), false);
            SavedModelBundle model = SavedModelBundle.load(Paths.get(modelDir, "model").toString(), "serve");
            return new DependencyParser(extractor, model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        model.close();
    }

    public static void main(String[] args) {
        try (DependencyParser model = DependencyParser.fromDirectory("semparse-tf4j/src/main/resources/dep-model")) {

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print(">>> ");
                String line = scanner.nextLine();
                if (line.equals("QUIT")) {
                    break;
                }
                List<String> words = Arrays.asList(line.split("\\s+"));

                Fields seq = new Fields();
                seq.add("word", words);

                ITokenSequence result = model.predictBatch(Collections.singletonList(seq)).get(0);
                List<String> lines = new ArrayList<>();
                for (IToken token : result) {
                    lines.add(String.format("%s\t%s\t%s\t%s", token.field(Fields.DefaultFields.TEXT), token.field("pos"),
                            token.field("dep"), token.field("head")));
                }
                System.out.println("\n" + String.join("\n", lines) + "\n");
            }
        }
    }


}
