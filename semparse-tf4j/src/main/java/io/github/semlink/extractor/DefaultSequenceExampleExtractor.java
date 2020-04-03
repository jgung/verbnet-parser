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

import org.tensorflow.example.Feature;
import org.tensorflow.example.FeatureList;
import org.tensorflow.example.FeatureLists;
import org.tensorflow.example.Features;
import org.tensorflow.example.SequenceExample;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.semlink.type.HasFields;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Aggregate feature extractor.
 *
 * @author jgung
 */
@AllArgsConstructor
public class DefaultSequenceExampleExtractor implements SequenceExampleExtractor {

    private List<Extractor<FeatureList>> featureListExtractors;
    private List<Extractor<Feature>> featureExtractors;
    private Map<String, Vocabulary> vocabularyMap;

    @Override
    public SequenceExample extractSequence(HasFields sequence) {
        FeatureLists.Builder featureLists = FeatureLists.newBuilder();
        for (Extractor<FeatureList> featureListExtractor : featureListExtractors) {
            featureLists.putFeatureList(featureListExtractor.name(), featureListExtractor.extract(sequence));
        }
        Features.Builder features = Features.newBuilder();
        for (Extractor<Feature> featureExtractor : featureExtractors) {
            features.putFeature(featureExtractor.name(), featureExtractor.extract(sequence));
        }
        return SequenceExample.newBuilder()
                .setContext(features)
                .setFeatureLists(featureLists)
                .build();
    }

    @Override
    public Optional<Vocabulary> vocabulary(@NonNull String key) {
        return Optional.ofNullable(vocabularyMap.get(key));
    }

}
