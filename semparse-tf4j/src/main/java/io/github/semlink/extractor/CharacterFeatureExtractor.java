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
import org.tensorflow.example.Int64List;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.semlink.extractor.config.FeatureSpec;
import io.github.semlink.type.HasFields;
import lombok.Getter;
import lombok.experimental.Accessors;

import static java.util.Collections.nCopies;

/**
 * Character feature extractor.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
public class CharacterFeatureExtractor extends BaseFeatureExtractor<FeatureList> {

    private String padWord;
    private String startWord;
    private String endWord;
    private int maxLength;
    private int leftPadding;
    private int rightPadding;

    public CharacterFeatureExtractor(FeatureSpec feature, Vocabulary vocabulary) {
        super(feature.name(), feature.key(), vocabulary);
        this.maxLength = feature.maxLen();
        this.leftPadding = feature.leftPadding();
        this.rightPadding = feature.rightPadding();
        this.padWord = feature.padWord();
        this.startWord = feature.leftPadWord();
        this.endWord = feature.rightPadWord();
    }

    @Override
    public FeatureList extract(HasFields seq) {
        FeatureList.Builder builder = FeatureList.newBuilder();
        getValues(seq).stream()
                .map(this::mapToChars)
                .map(this::featToIndex)
                .map(chars -> Feature.newBuilder().setInt64List(
                        Int64List.newBuilder().addAllValue(chars)))
                .forEach(builder::addFeature);
        return builder.build();
    }

    private List<Long> featToIndex(List<String> chars) {
        // add start padding
        List<Integer> result = new ArrayList<>(nCopies(leftPadding, vocabulary.featToIndex(startWord)));
        // add characters
        chars.stream().map(vocabulary::featToIndex).forEach(result::add);
        // add end padding
        result.addAll(nCopies(rightPadding, vocabulary.featToIndex(endWord)));

        // trim or pad resulting sequence
        if (result.size() < maxLength) {
            result.addAll(nCopies(maxLength - result.size(), vocabulary.featToIndex(padWord)));
        } else {
            result = result.subList(0, maxLength);
        }

        return result.stream().mapToLong(i -> i).boxed().collect(Collectors.toList());
    }

    private List<String> mapToChars(String original) {
        original = super.map(original);

        List<String> characters = new ArrayList<>();
        for (char c : original.toCharArray()) {
            characters.add(String.valueOf(c));
        }

        return characters;
    }

}
