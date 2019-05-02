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
