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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.semlink.PbVnMappings.MappedRoleset;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.verbnet.type.FramePhrase;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.NonNull;

/**
 * Applies mappings use PB-VN mappings.
 *
 * @author jgung
 */
public class RoleMappingAligner implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {

        Map<PropBankPhrase, FramePhrase> best = new HashMap<>();

        MappedRoleset mapped = null;
        for (MappedRoleset roleset : alignment.rolesets()) {

            Map<PropBankPhrase, FramePhrase> current = new HashMap<>();
            for (PropBankPhrase source : alignment.sourcePhrases()) {

                ArgNumber number = source.getNumber();
                if (!roleset.roleMappings().containsKey(number)) {
                    continue;
                }
                for (String mapping : roleset.roleMappings().get(number)) {
                    Optional<ThematicRoleType> roleType = ThematicRoleType.fromString(mapping);
                    if (!roleType.isPresent()) {
                        continue;
                    }
                    Optional<FramePhrase> framePhrase = alignment.byRole(roleType.get());
                    if (framePhrase.isPresent()) {
                        current.put(source, framePhrase.get());
                        break;
                    }
                }
            }
            if (current.size() > best.size()) {
                best = current;
                mapped = roleset;
            }
        }

        alignment.roleset(mapped);
        for (Map.Entry<PropBankPhrase, FramePhrase> aligned : best.entrySet()) {
            alignment.add(aligned.getKey(), aligned.getValue());
        }

    }

}
