package io.github.semlink.app;

import java.util.List;

import lombok.NonNull;

/**
 * Sequence prediction interface.
 *
 * @author jgung
 */
public interface SequencePredictor<T> {

    /**
     * Predict labels for a given input sequence.
     *
     * @param input target input
     * @return list of labels
     */
    List<String> predict(@NonNull T input);

    /**
     * Predict labels for a given batch of input sequences.
     *
     * @param inputs input sequence batch
     * @return batched sequence labels
     */
    List<List<String>> predictBatch(@NonNull List<T> inputs);

}
