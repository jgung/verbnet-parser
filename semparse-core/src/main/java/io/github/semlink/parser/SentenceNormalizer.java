package io.github.semlink.parser;

import lombok.NonNull;

/**
 * Applies String-level preprocessing steps to a given input sentence, such as adding punctuation, normalizing contractions, etc.
 *
 * @author jgung
 */
public interface SentenceNormalizer {

    /**
     * Preprocess a given sentence, adding periods, normalizing words, or correcting common spelling/grammatical errors.
     *
     * @param sentence input sentence
     * @return normalized sentence
     */
    String normalize(@NonNull String sentence);

}
