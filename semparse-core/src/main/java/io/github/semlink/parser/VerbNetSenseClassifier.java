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

package io.github.semlink.parser;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.DefaultSensePrediction;
import io.github.clearwsd.DefaultSensePredictor;
import io.github.clearwsd.ParsingSensePredictor;
import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.parser.Nlp4jDependencyParser;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.verbnet.DefaultVnIndex;
import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnIndex;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * VerbNet-specific {@link ParsingSensePredictor} implementation with {@link VnClass} senses.
 *
 * @author jgung
 */
@AllArgsConstructor
public class VerbNetSenseClassifier implements ParsingSensePredictor<VnClass> {

    private ParsingSensePredictor<VnClass> basePredictor;
    private VnIndex verbNet;

    @Override
    public List<SensePrediction<VnClass>> predict(@NonNull DepTree depTree) {
        List<SensePrediction<VnClass>> senses = basePredictor.predict(depTree);
        return senses.stream().map(sense -> convert(sense, depTree)).collect(Collectors.toList());
    }

    @Override
    public List<SensePrediction<VnClass>> predict(List<String> tokens) {
        return predict(basePredictor.parse(tokens));
    }

    @Override
    public DepTree parse(@NonNull List<String> tokens) {
        return basePredictor.parse(tokens);
    }

    @Override
    public List<String> segment(@NonNull String document) {
        return basePredictor.segment(document);
    }

    @Override
    public List<String> tokenize(@NonNull String sentence) {
        return basePredictor.tokenize(sentence);
    }

    private SensePrediction<VnClass> convert(@NonNull SensePrediction<VnClass> sense, DepTree tree) {
        VnClass result = null;
        if (sense.sense() != null) {
            Set<VnClass> senses = verbNet
                    .getByBaseIdAndLemma(sense.id(), tree.get(sense.index()).feature(FeatureType.Lemma));
            if (!senses.isEmpty()) {
                result = senses.stream().findFirst().get();
            }
        }
        return new DefaultSensePrediction<>(sense.index(), sense.originalText(), sense.id(), result);
    }

    /**
     * Initialize from a given {@link io.github.clearwsd.SensePredictor} model path and {@link VnIndex} lexicon.
     */
    public static VerbNetSenseClassifier fromModelPath(@NonNull String modelPath, @NonNull VnIndex verbNet) {
        DefaultSensePredictor<VnClass> predictor = DefaultSensePredictor.loadFromResource(modelPath,
                new Nlp4jDependencyParser());
        return new VerbNetSenseClassifier(predictor, verbNet);
    }

    /**
     * Initialize from a given {@link io.github.clearwsd.SensePredictor} model path.
     */
    public static VerbNetSenseClassifier fromModelPath(@NonNull String modelPath) {
        return fromModelPath(modelPath, new DefaultVnIndex());
    }

}
