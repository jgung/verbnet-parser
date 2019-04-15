package io.github.semlink.extractor;

/**
 * Indicates when a vocabulary exists that should be initialized or trained.
 *
 * @author jgung
 */
public interface HasVocabulary {

    /**
     * Vocabulary mapping from indices to features and from features to indices.
     */
    Vocabulary vocabulary();

}
