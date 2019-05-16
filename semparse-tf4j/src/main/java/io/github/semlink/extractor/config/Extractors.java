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


import io.github.semlink.extractor.SequenceExampleExtractor;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FeatureList;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.semlink.extractor.CharacterFeatureExtractor;
import io.github.semlink.extractor.ConstantFeatureExtractor;
import io.github.semlink.extractor.Extractor;
import io.github.semlink.extractor.KeyFeatureListExtractor;
import io.github.semlink.extractor.LengthExtractor;
import io.github.semlink.extractor.ScalarExtractor;
import io.github.semlink.extractor.DefaultSequenceExampleExtractor;
import io.github.semlink.extractor.TextExtractor;
import io.github.semlink.extractor.Vocabulary;

/**
 * Extractor factory.
 *
 * @author jgung
 */
public final class Extractors {

    public static SequenceExampleExtractor createExtractor(ExtractorSpec extractorSpec,
                                                           String vocabPath,
                                                           boolean includeTargets) {

        List<Extractor<Feature>> featureExtractors = new ArrayList<>();
        List<Extractor<FeatureList>> featureListExtractors = new ArrayList<>();

        extractFeatures(extractorSpec.features(), featureExtractors, featureListExtractors, vocabPath);
        if (includeTargets) {
            extractFeatures(extractorSpec.targets(), featureExtractors, featureListExtractors, vocabPath);
        }

        featureExtractors.add(new ConstantFeatureExtractor("sentence_idx", 0));
        featureExtractors.add(new LengthExtractor(extractorSpec.seqFeat()));

        return new DefaultSequenceExampleExtractor(featureListExtractors, featureExtractors);
    }

    private static void extractFeatures(List<FeatureSpec> features,
                                        List<Extractor<Feature>> featureExtractors,
                                        List<Extractor<FeatureList>> featureListExtractors,
                                        String vocabPath) {
        for (FeatureSpec feature : features) {

            List<Function<String, String>> stringFunctions = feature.mappingFuncs().stream()
                    .map(Extractors::getStringFunction)
                    .collect(Collectors.toList());

            if (feature.name().equals("elmo")) {
                featureListExtractors.add(new TextExtractor(feature.name(), feature.key())
                        .mappingFunctions(stringFunctions));
            } else if (feature.rank() == 3) {
                Vocabulary vocabulary = Vocabulary.read(Paths.get(vocabPath, feature.name()).toString(), feature.unknownWord());
                featureListExtractors.add(new CharacterFeatureExtractor(feature, vocabulary)
                        .mappingFunctions(stringFunctions));
            } else if (feature.rank() == 2) {
                if (feature.numeric()) {
                    // list extractor
                } else {
                    Vocabulary vocabulary = Vocabulary.read(Paths.get(vocabPath, feature.name()).toString(), feature.unknownWord());
                    featureListExtractors.add(new KeyFeatureListExtractor(feature, vocabulary)
                            .mappingFunctions(stringFunctions));
                }
            } else if (feature.rank() == 1) {
                // extractor if numeric
                if (feature.numeric()) {
                    featureExtractors.add(new ScalarExtractor(feature.name(), feature.key()));
                }
                // feature if not
            } else {
                throw new IllegalArgumentException("Unexpected feature rank: " + feature.rank());
            }
        }
    }

    private static Function<String, String> getStringFunction(String functionName) {
        switch (functionName) {
            case "lower":
                return String::toLowerCase;
            case "digit_norm":
                return s -> s.replaceAll("\\d", "#");
            default:
                throw new IllegalArgumentException("Unrecognized string function name: " + functionName);
        }
    }
}
