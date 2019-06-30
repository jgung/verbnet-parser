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

import java.util.Set;
import java.util.stream.Collectors;

import io.github.semlink.verbnet.restrictions.DefaultVnRestrictions;
import io.github.semlink.verbnet.restrictions.VnRestrictions;
import io.github.semlink.verbnet.syntax.VnPrep;
import lombok.NonNull;

/**
 * Preposition within a VerbNet syntactic frame.
 *
 * @author jgung
 */
public class Preposition extends FramePhrase {

    private VnPrep prep;

    public Preposition(@NonNull VnPrep prep) {
        super(VerbNetSyntaxType.PREP);
        this.prep = prep;
    }

    /**
     * Return valid prepositions for this phrase.
     */
    public Set<PrepType> valid() {
        Set<PrepType> types = prep.types().stream().map(PrepType::fromString).collect(Collectors.toSet());
        if (types.contains(PrepType.TO)) {
            types.addAll(PrepType.to());
        }
        return types;
    }

    /**
     * Return selectional restrictions for this preposition.
     *
     * @param valid if True, return valid types , if False return excluded types
     * @return set of selectional restriction types
     */
    public Set<PrepSelRelType> selectionalRes(boolean valid) {
        return DefaultVnRestrictions.map(prep.restrictions(), PrepSelRelType::fromString).stream()
                .map(valid ? VnRestrictions::include : VnRestrictions::exclude)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return type() + "[" + valid().stream().map(Enum::name).collect(Collectors.joining(" | ")) + "]";
    }
}