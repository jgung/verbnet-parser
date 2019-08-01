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
import java.util.stream.Collectors;

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

import static io.github.semlink.app.ParsingUtils.fixCycles;
import static io.github.semlink.app.ParsingUtils.getLabels;
import static io.github.semlink.app.ParsingUtils.toArcProbs;
import static io.github.semlink.app.ParsingUtils.toRelProbs;
import static io.github.semlink.app.ParsingUtils.trimToLength;
import static io.github.semlink.tensor.Tensors.batchExamples;
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
                List<String> words = inputs.get(i).field("word");

                List<IToken> tokens = new ArrayList<>();

                float[][] arcs = trimToLength(arcProbs.get(i), words.size() + 1);
                float[][][] rels = trimToLength(relProbs.get(i), words.size() + 1);
                List<String> tags = posResults.get(i);
                List<Integer> edges = fixCycles(arcs);
                List<String> labels = getLabels(rels, edges, word -> relVocabulary.indexToFeat(word));

                for (int idx = 1, len = words.size() + 1; idx < len; ++idx) {
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
        String path = args[0];

        try (DependencyParser model = DependencyParser.fromDirectory(path)) {

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
