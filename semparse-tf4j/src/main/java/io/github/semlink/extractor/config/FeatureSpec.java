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

package io.github.semlink.extractor.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.semlink.extractor.Vocabulary;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * JSON-serializable feature configuration.
 *
 * @author jgung
 */
@Getter
@Setter
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FeatureSpec {

    /** name used to instantiate this feature. */
    private String name;

    /** key used for lookup during feature extraction. */
    private String key;

    /** string mapping functions applied during extraction. */
    @JsonProperty("mapping_funcs")
    private List<String> mappingFuncs = new ArrayList<>();

    /** padding functions applied during extraction. */
    @JsonProperty("padding_funcs")
    private List<String> paddingFuncs = new ArrayList<>();

    /** 2 most common feature rank for our NLP applications (word/token-level features). */
    private int rank = 2;

    /** indicates whether this is a numeric or sparse lookup feature. */
    private boolean numeric = false;

    /** maximum sequence length of feature. */
    @JsonProperty("max_len")
    private int maxLen = -1;

    /** word used to replace OOV tokens. */
    @JsonProperty("unknown_word")
    private String unknownWord = Vocabulary.UNKNOWN_WORD;

    /** padding token. */
    @JsonProperty("pad_word")
    private String padWord = Vocabulary.PAD_WORD;

    /** number of tokens to use for left padding. */
    @JsonProperty("left_padding")
    private int leftPadding = 0;

    /** number of tokens to use for right padding. */
    @JsonProperty("right_padding")
    private int rightPadding = 0;

    /** word used for left padding. */
    @JsonProperty("left_pad_word")
    private String leftPadWord = Vocabulary.START_WORD;

    /** word used for right padding. */
    @JsonProperty("right_pad_word")
    private String rightPadWord = Vocabulary.END_WORD;

    /** pre-initialized vocabulary. */
    private Map<String, Integer> indices;

}
