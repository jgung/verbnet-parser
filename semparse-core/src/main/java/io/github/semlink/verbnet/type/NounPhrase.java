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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.semlink.verbnet.restrictions.DefaultVnRestrictions;
import io.github.semlink.verbnet.restrictions.VnRestrictions;
import io.github.semlink.verbnet.syntax.VnNounPhrase;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Noun Phrase (NP) within a VerbNet syntactic frame.
 *
 * @author jgung
 */
@Accessors(fluent = true)
public class NounPhrase extends FramePhrase {

    private VnNounPhrase vnNounPhrase;

    @Setter
    private Preposition preposition;

    public NounPhrase(@NonNull VnNounPhrase vnNounPhrase) {
        super(VerbNetSyntaxType.NP);
        this.vnNounPhrase = vnNounPhrase;
    }

    /**
     * Preposition associated with this NP such as in a mapped VerbNet frame, e.g. "to" in "[to] NP".
     */
    public Optional<Preposition> preposition() {
        return Optional.ofNullable(preposition);
    }

    /**
     * VerbNet thematic role type for this NP.
     */
    public ThematicRoleType thematicRoleType() {
        return ThematicRoleType.fromString(vnNounPhrase.thematicRole()).orElse(ThematicRoleType.NONE);
    }

    /**
     * Return syntactic restrictions for this noun phrase.
     *
     * @param valid if True, return valid types, if False return excluded types
     * @return set of syntactic restriction types
     */
    public Set<NpSynRes> syntacticRes(boolean valid) {
        return DefaultVnRestrictions.map(vnNounPhrase.syntacticRestrictions(), NpSynRes::fromString).stream()
                .map(valid ? DefaultVnRestrictions::include : DefaultVnRestrictions::exclude)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Return selectional restrictions for this noun phrase.
     *
     * @param valid if True, return valid types , if False return excluded types
     * @return set of selectional restriction types
     */
    public Set<NpSelRes> selectionalRes(boolean valid) {
        return DefaultVnRestrictions.map(vnNounPhrase.selectionalRestrictions(), NpSelRes::fromString).stream()
                .map(valid ? VnRestrictions::include : VnRestrictions::exclude)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        String result = type() + "[" + thematicRoleType().name();
        Set<NpSynRes> plus = syntacticRes(true);
        Set<NpSynRes> minus = syntacticRes(false);
        if (plus.size() > 0) {
            result += "+" + plus.stream().sorted().map(Objects::toString).collect(Collectors.joining(", "));
        }
        if (minus.size() > 0) {
            result += "-" + minus.stream().sorted().map(Objects::toString).collect(Collectors.joining(", "));
        }
        result += "]";
        if (preposition().isPresent()) {
            result = preposition.toString() + " " + result;
        }
        return result;
    }
}
