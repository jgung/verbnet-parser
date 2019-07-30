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

package io.github.semlink.tensor;

import org.tensorflow.Tensor;
import org.tensorflow.example.SequenceExample;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.NonNull;

/**
 * Tensor utilities.
 *
 * @author jgung
 */
public final class Tensors {

    public static byte[][] batchExamples(@NonNull List<SequenceExample> examples) {
        byte[][] batch = new byte[examples.size()][];
        int index = 0;
        for (SequenceExample example : examples) {
            batch[index++] = example.toByteArray();
        }
        return batch;
    }

    public static List<List<String>> toStringLists(@NonNull Tensor<?> tensor) {
        long[] shape = tensor.shape();

        byte[][][] bytes;
        if (shape.length == 2) {
            int batchSize = (int) shape[0];
            int dim = (int) shape[1];
            bytes = tensor.copyTo(new byte[batchSize][dim][]);
        } else if (shape.length == 1) {
            int dim = (int) shape[0];
            byte[][][] result = new byte[1][dim][];
            tensor.copyTo(result[0]);
            bytes = result;
        } else {
            throw new IllegalArgumentException("Tensor rank is " + shape.length + ", was expecting 2 or 1.");
        }

        List<List<String>> results = new ArrayList<>();
        for (byte[][] list : bytes) {
            results.add(toStringList(list));
        }

        return results;
    }

    public static List<float[][]> toArcProbs(@NonNull Tensor<?> tensor) {
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

    public static List<float[][][]> toRelProbs(@NonNull Tensor<?> tensor) {
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

    public static List<String> toStringList(@NonNull byte[][] bytes) {
        List<String> result = new ArrayList<>();
        for (byte[] word : bytes) {
            result.add(new String(word, StandardCharsets.UTF_8));
        }
        return result;
    }

}
