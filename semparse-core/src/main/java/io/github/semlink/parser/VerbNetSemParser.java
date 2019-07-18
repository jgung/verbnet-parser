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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.semlink.app.Span;
import io.github.semlink.propbank.type.FunctionTag;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.VerbNetAligner;
import io.github.semlink.verbnet.VnClass;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import static io.github.semlink.parser.SemanticRoleLabeler.convert;

/**
 * VerbNet (shallow) semantic parser. Performs predicate mapping (light verb to nominal predicate), semantic role labeling and
 * alignment of roles to VerbNet thematic roles/frames.
 *
 * @author jgung
 */
@AllArgsConstructor
public class VerbNetSemParser {

    private SemanticRoleLabeler<PropBankArg> roleLabeler;
    private VerbNetAligner aligner;
    private List<PredicateMapper<VnClass>> predicateMappers;

    /**
     * Perform a shallow semantic parse on the input dependency parse for a given list of predicates.
     *
     * <p/> 1. Identify potential light verbs and heavy nouns
     * <p/> 2. Perform semantic role labeling with respect to identified verbal and nominal predicates
     * <p/> 3. If resulting roles are correct, prefer heavy noun predicates over light verb predicates
     *
     * @param parsed dependency parse
     * @param senses predicates with sense predictions
     * @return extracted VerbNet propositions/shallow semantic parse
     */
    public List<VerbNetProp> extractProps(@NonNull DepTree parsed,
                                          @NonNull List<SensePrediction<VnClass>> senses) {
        List<Integer> predicateIndices = new ArrayList<>();
        List<VerbAndNoun> lightVerbs = new ArrayList<>();

        for (SensePrediction<VnClass> sense : senses) {
            DepNode verb = parsed.get(sense.index());

            // map light verbs to nominal props
            Optional<Span<VnClass>> possibleHeavyNoun = predicateMappers.stream()
                    .map(mapper -> mapper.mapPredicate(verb))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            VerbAndNoun instance = new VerbAndNoun()
                    .verbIndex(predicateIndices.size())
                    .verbClass(sense.sense());

            predicateIndices.add(sense.index());
            lightVerbs.add(instance);

            possibleHeavyNoun.ifPresent(heavyNoun -> {
                instance.nominalIndex(predicateIndices.size())
                        .nominalClass(heavyNoun.label());
                predicateIndices.add(heavyNoun.startIndex());
            });
        }

        List<Proposition<DepNode, PropBankArg>> props = roleLabeler.parse(parsed, predicateIndices);

        List<Proposition<VnClass, PropBankArg>> filtered = new ArrayList<>();
        for (VerbAndNoun lv : lightVerbs) {
            if (lv.nominalClass == null) {
                filtered.add(convert(props.get(lv.verbIndex), lv.verbClass));
                continue;
            }

            Proposition<DepNode, PropBankArg> nominalProp = props.get(lv.nominalIndex);
            if (nominalProp.arguments().spans().stream().anyMatch(arg -> arg.label().getFunctionTag() == FunctionTag.LVB)) {
                // exclude any props without LVB annotated–this indicates the role labels were not correct
                filtered.add(convert(nominalProp, lv.nominalClass));
            } else {
                // fallback to verbal proposition if nominal event structure not parsed correctly
                filtered.add(convert(props.get(lv.verbIndex), lv.verbClass));
            }
        }

        return aligner.align(parsed, filtered);
    }

    @Setter
    @Accessors(fluent = true)
    private static class VerbAndNoun {
        int verbIndex;
        int nominalIndex;
        VnClass verbClass;
        VnClass nominalClass;
    }

}
