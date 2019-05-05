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

package io.github.semlink.verbnet.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.verbnet.restrictions.DefaultVnRestrictions;
import io.github.clearwsd.verbnet.syntax.VnPrep;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(fluent = true)
public class Preposition extends FramePhrase {

    private Set<PrepType> valid = Collections.emptySet();
    private Set<PrepSelRelType> include = new HashSet<>();
    private Set<PrepSelRelType> exclude = new HashSet<>();

    public Preposition() {
        super(VerbNetSyntaxType.PREP);
    }

    public static Preposition of(@NonNull VnPrep prepArgDesc) {
        Preposition preposition = new Preposition();
        preposition.valid(prepArgDesc.types().stream().map(PrepType::fromString).collect(Collectors.toSet()));

        List<DefaultVnRestrictions<PrepSelRelType>> restrictions = DefaultVnRestrictions
                .map(prepArgDesc.restrictions(), PrepSelRelType::fromString);
        if (restrictions.size() > 0) {
            preposition.exclude(restrictions.get(0).exclude());
            preposition.include(restrictions.get(0).include());
        }
        return preposition;
    }

    @Override
    public String toString() {
        return type() + "[" + valid().stream().map(Enum::name).collect(Collectors.joining(" | ")) + "]";
    }
}