package io.github.semlink.extractor;

import io.github.semlink.type.HasFields;

/**
 * Generic extractor interface.
 *
 * @param <T> generic extraction result
 * @author jgung
 */
public interface Extractor<T> {

    /**
     * Unique name of this extractor, used as a placeholder.
     */
    String name();

    /**
     * Extract a value from an input sequence.
     *
     * @param nlpSeq input sequence
     * @return extracted value
     */
    T extract(HasFields nlpSeq);

}
