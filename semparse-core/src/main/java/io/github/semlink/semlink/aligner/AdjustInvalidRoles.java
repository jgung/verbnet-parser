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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.semlink.propbank.frames.PbRole;
import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.semlink.PropBankPhrase;
import lombok.NonNull;

/**
 * Adjust invalid roles (decrement/increment numbered arguments).
 *
 * @author jgung
 */
public class AdjustInvalidRoles implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {

        if (alignment.roleset() == null) {
            return;
        }

        Set<ArgNumber> valid = alignment.roleset().roles().roles().stream()
                .map(PbRole::number)
                .filter(r -> !alignment.sourcePhrases(true).stream()
                        .map(PropBankPhrase::getNumber)
                        .collect(Collectors.toSet()).contains(r))
                .collect(Collectors.toSet());

        Optional<PropBankPhrase> decrement = alignment.sourcePhrases(false).stream()
                .filter(phrase -> {
                    if (phrase.isModifier()) {
                        return false;
                    }
                    if (valid.contains(phrase.argument().getNumber())) {
                        return false;
                    }
                    int ordinal = phrase.argument().getNumber().ordinal() - 1;
                    if (ordinal < 0) {
                        return false;
                    }
                    ArgNumber minusOne = ArgNumber.values()[ordinal];
                    return valid.contains(minusOne);
                })
                .findFirst();

        decrement.ifPresent(phrase -> {
            int ordinal = phrase.argument().getNumber().ordinal() - 1;
            ArgNumber minusOne = ArgNumber.values()[ordinal];
            phrase.setNumber(minusOne);
            new RoleMappingAligner().align(alignment);
        });
    }

}
