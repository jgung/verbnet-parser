/*
 * Copyright 2020 James Gung
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

package io.github.semlink.dep.util;

import org.tensorflow.Tensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Dependency parsing utility class.
 *
 * @author jgung
 */
@UtilityClass
public class ParsingUtils {

    public List<float[][]> toArcProbs(@NonNull Tensor<?> tensor) {
        long[] shape = tensor.shape();

        float[][][] arcProbs;
        if (shape.length == 3) {
            int batchSize = (int) shape[0];
            int depDim = (int) shape[1];
            int parDim = (int) shape[2];
            arcProbs = tensor.copyTo(new float[batchSize][depDim][parDim]);
        } else {
            throw new IllegalArgumentException("Tensor rank is " + shape.length + ", was expecting 3.");
        }

        return new ArrayList<>(Arrays.asList(arcProbs));
    }

    public List<float[][][]> toRelProbs(@NonNull Tensor<?> tensor) {
        long[] shape = tensor.shape();

        float[][][][] arcProbs;
        if (shape.length == 4) {
            int batchSize = (int) shape[0];
            int depDim = (int) shape[1];
            int relDim = (int) shape[2];
            int parDim = (int) shape[3];
            arcProbs = tensor.copyTo(new float[batchSize][depDim][relDim][parDim]);
        } else {
            throw new IllegalArgumentException("Tensor rank is " + shape.length + ", was expecting 4.");
        }

        return new ArrayList<>(Arrays.asList(arcProbs));
    }

    public List<String> getLabels(float[][][] relProbs, List<Integer> edges, Function<Integer, String> reverseVocab) {
        List<String> labels = new ArrayList<>();
        for (int t = 0; t < edges.size(); ++t) {
            int edgeIndex = edges.get(t);
            int argmax = 0;
            float max = -Float.MAX_VALUE;
            int index = 0;
            for (float[] labelProbByHead : relProbs[t]) {
                float val = labelProbByHead[edgeIndex];
                if (val > max) {
                    argmax = index;
                    max = val;
                }
                ++index;
            }
            labels.add(reverseVocab.apply(argmax));
        }
        return labels;
    }

    public float[][] trimToLength(float[][] squareMatrix, int length) {
        float[][] newMatrix = new float[length][];
        for (int i = 0; i < length; ++i) {
            float[] newRow = new float[length];
            System.arraycopy(squareMatrix[i], 0, newRow, 0, length);
            newMatrix[i] = newRow;
        }
        return newMatrix;
    }

    public float[][][] trimToLength(float[][][] tensor, int length) {
        float[][][] newTensor = new float[length][][];

        for (int i = 0; i < length; ++i) {
            float[][] matrix = tensor[i];
            float[][] newMatrix = new float[matrix.length][];
            int rowIndex = 0;
            for (float[] row : matrix) {
                float[] newRow = new float[length];
                System.arraycopy(row, 0, newRow, 0, length);
                newMatrix[rowIndex++] = newRow;
            }
            newTensor[i] = newMatrix;
        }

        return newTensor;
    }

    public List<Integer> fixCycles(float[][] probs) {
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

    private float scoreEdges(float[][] probs, List<Integer> edges) {
        float sum = 0;
        for (int v = 1; v < probs.length; ++v) {
            sum += Math.log(probs[v][edges.get(v)]);
        }
        return sum;
    }

    private float[][] makeRoot(float[][] probs, int root) {
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

    private List<Integer> findRoots(List<Integer> edges) {
        return IntStream.range(0, edges.size())
                .filter(i -> i > 0 && edges.get(i) == 0)
                .boxed()
                .collect(Collectors.toList());
    }

    private List<Integer> greedy(float[][] probs) {

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

    private void normalizeInPlace(float[] values) {
        float sum = 0;
        for (float val : values) {
            sum += val;
        }
        for (int i = 0; i < values.length; ++i) {
            values[i] /= sum;
        }
    }

    private int argmax(float[] values) {
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

    @SuppressWarnings("RedundantModifiersUtilityClassLombok")
    private static class TarjanCycleFinder {

        int[] edges;
        List<Integer> vertices;
        List<Integer> indices;
        List<Integer> lowlinks;
        Stack<Integer> stack;
        List<Boolean> onstack;
        int currentIndex;
        List<List<Integer>> cycles;

        TarjanCycleFinder(int[] edges) {
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

    private List<List<Integer>> findCycles(int[] edges) {
        return new TarjanCycleFinder(edges).findCycles();
    }

}
