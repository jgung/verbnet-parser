package io.github.semlink.tensor;

import org.tensorflow.Tensor;
import org.tensorflow.example.SequenceExample;

import java.nio.charset.Charset;
import java.util.ArrayList;
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

    public static List<String> toStringList(@NonNull byte[][] bytes) {
        List<String> result = new ArrayList<>();
        for (byte[] word : bytes) {
            result.add(new String(word, Charset.defaultCharset()));
        }
        return result;
    }

}
