package io.github.semlink.extractor;

import java.util.List;

import io.github.semlink.type.HasFields;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Base feature extractor with a vocabulary.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
public abstract class BaseFeatureExtractor<T> extends KeyExtractor<T> implements HasVocabulary {

    protected Vocabulary vocabulary;
    protected String oov;

    public BaseFeatureExtractor(String name, String key, Vocabulary vocabulary) {
        super(name, key);
        this.vocabulary = vocabulary;
    }

    protected List<String> getValues(HasFields nlpSeq) {
        return nlpSeq.field(key);
    }

}
