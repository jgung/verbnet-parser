package io.github.semlink.app;

import java.util.List;

import lombok.NonNull;

/**
 * Generic shallow parser interface.
 *
 * @author jgung
 */
public interface IShallowParser<T, S extends List<T>> extends AutoCloseable {

    /**
     * Produce a list of phrases from a given sequence of tokens.
     *
     * @param sequence token sequence
     * @return list of phrases
     */
    Chunking<String> shallowParse(@NonNull S sequence);

    /**
     * Produce a list of phrases from a given batch of sequences of tokens.
     *
     * @param sequence token sequences
     * @return batched lists of phrases
     */
    List<Chunking<String>> shallowParseBatch(@NonNull List<S> sequence);

}
