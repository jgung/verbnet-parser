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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.clearwsd.DefaultSensePrediction;
import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.app.Span;
import io.github.semlink.verbnet.VnClass;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Default {@link VnPredicateDetector} implementation.
 *
 * @author jgung
 */
@AllArgsConstructor
public class DefaultVnPredicateDetector implements VnPredicateDetector {

    private VerbNetSenseClassifier verbNetClassifier;
    private PredicateMapper<VnClass> lightVerbMapper;
    private List<PredicateMapper<VnClass>> tokenMappers;

    @SafeVarargs
    public DefaultVnPredicateDetector(VerbNetSenseClassifier verbNetClassifier, PredicateMapper<VnClass> lightVerbMapper,
                                      PredicateMapper<VnClass>... predicateMappers) {
        this.verbNetClassifier = verbNetClassifier;
        this.lightVerbMapper = lightVerbMapper;
        this.tokenMappers = Arrays.stream(predicateMappers).collect(Collectors.toList());
    }

    @Override
    public List<SensePrediction<VnClass>> detectPredicates(@NonNull DepTree depTree) {
        List<SensePrediction<VnClass>> senses = verbNetClassifier.predict(depTree);

        Map<Integer, SensePrediction<VnClass>> predictions = senses.stream()
                // TODO: VerbNet classifier should ideally have this kind of check
                .filter(sense -> !depTree.get(sense.index()).feature(FeatureType.Dep).toString().equalsIgnoreCase("nmod"))
                .collect(Collectors.toMap(SensePrediction::index, Function.identity()));

        for (SensePrediction<VnClass> sense : senses) {
            DepNode verb = depTree.get(sense.index());

            // map light verbs to nominal props
            lightVerbMapper.mapPredicate(verb).map(span -> toSensePrediction(span, depTree))
                    .ifPresent(prediction -> {
                        if (!predictions.containsKey(prediction.index())) {
                            predictions.put(prediction.index(), prediction);
                        }
                    });
        }

        for (PredicateMapper<VnClass> mapper : tokenMappers) {
            for (DepNode depNode : depTree) {
                mapper.mapPredicate(depNode).map(span -> toSensePrediction(span, depTree))
                        .ifPresent(prediction -> {
                            if (!predictions.containsKey(prediction.index())) {
                                predictions.put(prediction.index(), prediction);
                            }
                        });
            }
        }

        return predictions.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    private static SensePrediction<VnClass> toSensePrediction(Span<VnClass> span, DepTree depTree) {
        return new DefaultSensePrediction<>(span.startIndex(),
                span.get(depTree).stream()
                        .map(node -> (String) node.feature(FeatureType.Text))
                        .collect(Collectors.joining(" ")), span.label().verbNetId().classId(),
                span.label());
    }

}
