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

package io.github.semlink.semlink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnMember;
import io.github.semlink.verbnet.semantics.EventArgument;
import io.github.semlink.verbnet.semantics.SemanticPredicate;
import io.github.semlink.verbnet.semantics.ThematicRoleArgument;
import io.github.semlink.verbnet.semantics.VerbSpecificArgument;
import io.github.semlink.verbnet.type.FramePhrase;
import io.github.semlink.verbnet.type.SemanticArgumentType;
import io.github.semlink.verbnet.type.SemanticPredicateType;
import io.github.semlink.verbnet.type.SyntacticFrame;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.NonNull;

/**
 * VerbNet semantic predicate extraction service.
 *
 * @author jgung
 */
public class VnPredicateExtractor {

    /**
     * Extract {@link SemanticPredicate VerbNet semantic predicates} for a given alignment.
     *
     * @param alignment alignment
     * @param frame     aligned frame
     * @param vnClass   VerbNet class
     * @param lemma     Verb lemma
     * @return extracted semantic representations
     */
    public List<SemanticPredicate> parsePredicates(@NonNull Alignment<PropBankPhrase, FramePhrase> alignment,
                                                   @NonNull SyntacticFrame frame,
                                                   @NonNull VnClass vnClass,
                                                   @NonNull String lemma) {
        List<SemanticPredicate> predicates = frame.frame().predicates().stream()
                .map(SemanticPredicate::of)
                .collect(Collectors.toList());

        Map<ThematicRoleType, ThematicRoleType> equalsRoles = new HashMap<>();
        for (SemanticPredicate predicate : predicates) {
            if (predicate.type() == SemanticPredicateType.EQUALS) {
                List<ThematicRoleArgument<PropBankPhrase>> args = predicate.get(SemanticArgumentType.THEMROLE);
                if (args.size() == 2) {
                    equalsRoles.put(args.get(0).thematicRoleType(), args.get(1).thematicRoleType());
                    equalsRoles.put(args.get(1).thematicRoleType(), args.get(0).thematicRoleType());
                }
            }
        }

        List<SemanticPredicate> filtered = new ArrayList<>();
        for (SemanticPredicate predicate : predicates) {
            if (predicate.type() == SemanticPredicateType.EQUALS) {
                continue;
            }
            List<EventArgument<VnClass>> args = predicate.get(SemanticArgumentType.EVENT);
            for (EventArgument<VnClass> arg : args) {
                arg.variable(vnClass);
            }

            List<ThematicRoleArgument<PropBankPhrase>> roles = predicate.get(SemanticArgumentType.THEMROLE);
            for (ThematicRoleArgument<PropBankPhrase> role : roles) {
                Optional<FramePhrase> phrase = frame.role(role.thematicRoleType());
                phrase.ifPresent(framePhrase -> role.variable(alignment.getSource(framePhrase)));
                if (!phrase.isPresent() && equalsRoles.containsKey(role.thematicRoleType())) {
                    ThematicRoleType equivalentRole = equalsRoles.get(role.thematicRoleType());
                    phrase = frame.role(equivalentRole);
                    phrase.ifPresent(framePhrase -> role.variable(alignment.getSource(framePhrase)));
                }
            }
            filtered.add(predicate);

            // assign verb-specific features ----------------------------------------------------------------
            List<String> features = vnClass.members().stream()
                    .filter(member -> member.name().equals(lemma))
                    .map(VnMember::features)
                    .findFirst().orElse(Collections.emptyList());
            if (features.isEmpty()) {
                continue;
            }
            List<VerbSpecificArgument<String>> vsa = predicate.get(SemanticArgumentType.VERBSPECIFIC);
            for (VerbSpecificArgument<String> role : vsa) {
                role.variable(String.join(", ", features));
            }
        }

        return filtered;
    }

}
