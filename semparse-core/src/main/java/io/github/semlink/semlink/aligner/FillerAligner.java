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

package io.github.semlink.semlink.aligner;

import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.verbnet.type.NounPhrase;
import io.github.semlink.verbnet.type.ThematicRoleType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * Just map remaining numbered args to any remaining valid frame roles.
 *
 * @author jgung
 */
public class FillerAligner implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {

        // TODO: this seems like a hack
        boolean noAgentiveA0 = alignment.proposition().predicate().related().stream()
            .allMatch(s -> s.roles().stream()
                .map(r -> ThematicRoleType.fromString(r.type()).orElse(ThematicRoleType.NONE))
                .noneMatch(ThematicRoleType::isAgentive));

        for (PropBankPhrase phrase : alignment.sourcePhrases(false)) {
            List<NounPhrase> unaligned = alignment.targetPhrases(false).stream()
                .filter(i -> i instanceof NounPhrase)
                .map(i -> ((NounPhrase) i))
                .collect(Collectors.toList());
            if (phrase.getNumber() == ArgNumber.A0) {
                // TODO: seems like a hack
                if (alignment.proposition().predicate().verbNetId().classId().startsWith("51") && noAgentiveA0) {
                    for (NounPhrase unalignedPhrase : unaligned) {
                        if (unalignedPhrase.thematicRoleType() == ThematicRoleType.THEME) {
                            alignment.add(phrase, unalignedPhrase);
                            break;
                        }
                    }
                } else {
                    for (NounPhrase unalignedPhrase : unaligned) {
                        if (unalignedPhrase.thematicRoleType() == ThematicRoleType.AGENT) {
                            alignment.add(phrase, unalignedPhrase);
                            break;
                        }
                    }
                }
            } else if (phrase.getNumber() == ArgNumber.A1) {
                for (NounPhrase unalignedPhrase : unaligned) {
                    if (unalignedPhrase.thematicRoleType() == ThematicRoleType.THEME
                        || unalignedPhrase.thematicRoleType() == ThematicRoleType.PATIENT) {
                        alignment.add(phrase, unalignedPhrase);
                        break;
                    }
                }
            } else if (phrase.getNumber() == ArgNumber.A3) {
                for (NounPhrase unalignedPhrase : unaligned) {
                    if (unalignedPhrase.thematicRoleType().isStartingPoint()) {
                        alignment.add(phrase, unalignedPhrase);
                        break;
                    }
                }
            } else if (phrase.getNumber() == ArgNumber.A4) {
                for (NounPhrase unalignedPhrase : unaligned) {
                    if (unalignedPhrase.thematicRoleType().isEndingPoint()) {
                        alignment.add(phrase, unalignedPhrase);
                        break;
                    }
                }
            }


        }
    }

}
