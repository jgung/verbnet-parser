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
import java.util.stream.Collectors;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.verbnet.VnClass;
import io.github.semlink.app.Span;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.VerbNetAligner;
import lombok.AllArgsConstructor;
import lombok.NonNull;

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
     * @param parsed dependency parse
     * @param senses predicates with sense predictions
     * @return extracted VerbNet propositions/shallow semantic parse
     */
    public List<VerbNetProp> extractProps(@NonNull DepTree parsed,
                                          @NonNull List<SensePrediction<VnClass>> senses) {
        List<VnClass> vnClasses = new ArrayList<>();
        List<Integer> predicateIndices = new ArrayList<>();
        for (SensePrediction<VnClass> sense : senses) {
            DepNode verb = parsed.get(sense.index());

            // map light verbs to nominal props
            Optional<Span<VnClass>> possibleHeavyNoun = predicateMappers.stream()
                    .map(mapper -> mapper.mapPredicate(verb))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            if (possibleHeavyNoun.isPresent()) {
                vnClasses.add(possibleHeavyNoun.get().label());
                predicateIndices.add(possibleHeavyNoun.get().startIndex());
            } else {
                // otherwise just use verbal prop
                vnClasses.add(sense.sense());
                predicateIndices.add(sense.index());
            }
        }

        List<Proposition<DepNode, PropBankArg>> props = roleLabeler.parse(parsed, predicateIndices);
        List<DefaultVerbNetProp> vnProps = aligner.align(parsed, convert(props, vnClasses));
        return vnProps.stream().map(prop -> (VerbNetProp) prop).collect(Collectors.toList());
    }

}
