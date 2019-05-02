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

import io.github.clearwsd.verbnet.restrictions.DefaultVnRestrictions;
import io.github.clearwsd.verbnet.syntax.VnNounPhrase;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true)
public class NounPhrase extends FramePhrase {

    @Getter
    private ThematicRoleType thematicRoleType = ThematicRoleType.NONE;
    @Getter
    private Set<NounPhraseSynRelType> include = new HashSet<>();
    @Getter
    private Set<NounPhraseSynRelType> exclude = new HashSet<>();

    private Preposition preposition;

    public NounPhrase() {
        super(VerbNetSyntaxType.NP);
    }

    public Optional<Preposition> preposition() {
        return Optional.ofNullable(preposition);
    }

    public static NounPhrase of(@NonNull VnNounPhrase np) {
        NounPhrase nounPhrase = new NounPhrase();

        ThematicRoleType.fromString(np.thematicRole())
            .ifPresent(nounPhrase::thematicRoleType);

        List<DefaultVnRestrictions<NounPhraseSynRelType>> restrictions = DefaultVnRestrictions
            .map(np.syntacticRestrictions(), NounPhraseSynRelType::fromString);
        if (restrictions.size() > 0) {
            // TODO: just use selectional restrictions directly from VnNounPhrase
            nounPhrase.exclude(restrictions.get(0).exclude());
            nounPhrase.include(restrictions.get(0).include());
        }
        return nounPhrase;
    }

    @Override
    public String toString() {
        String result = type() + "[" + thematicRoleType.name() + "]";
        if (preposition().isPresent()) {
            result = preposition.toString() + " " + result;
        }
        return result;
    }
}
