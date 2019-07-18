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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.semlink.propbank.type.FunctionTag;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.VerbNetAligner;
import io.github.semlink.verbnet.VnClass;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import static io.github.semlink.parser.SemanticRoleLabeler.convert;

/**
 * VerbNet (shallow) semantic parser. Performs semantic role labeling and alignment of roles to VerbNet thematic roles/frames.
 *
 * @author jgung
 */
@AllArgsConstructor
public class VerbNetSemParser {

    private SemanticRoleLabeler<PropBankArg> roleLabeler;
    private VerbNetAligner aligner;

    /**
     * Perform a shallow semantic parse on the input dependency parse for a given list of predicates.
     *
     * @param parsed dependency parse
     * @param senses predicates with sense predictions
     * @return extracted VerbNet propositions/shallow semantic parse
     */
    public List<VerbNetProp> extractProps(@NonNull DepTree parsed,
                                          @NonNull List<SensePrediction<VnClass>> senses) {
        Map<Integer, SensePrediction<VnClass>> sensesByIndex = senses.stream()
                .collect(Collectors.toMap(SensePrediction::index, Function.identity()));

        List<Proposition<DepNode, PropBankArg>> props = roleLabeler.parse(parsed, senses.stream()
                .map(SensePrediction::index)
                .collect(Collectors.toList()));

        List<Proposition<VnClass, PropBankArg>> filtered = new ArrayList<>();
        for (Proposition<DepNode, PropBankArg> prop : props) {
            if (prop.relSpan() == null || prop.arguments().spans().size() == 1) {
                continue;
            }
            if (prop.arguments().spans().stream().anyMatch(arg -> arg.label().getFunctionTag() == FunctionTag.PRR
                    && prop.arguments().spans().size() <= 2)) {
                continue;
            }
            filtered.add(convert(prop, sensesByIndex.get(prop.relIndex()).sense()));
        }

        return aligner.align(parsed, filtered);
    }

}
