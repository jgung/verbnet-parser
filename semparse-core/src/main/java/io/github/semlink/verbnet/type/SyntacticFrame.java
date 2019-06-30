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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.semlink.verbnet.VnFrame;
import io.github.semlink.verbnet.syntax.VnSyntax;
import io.github.semlink.verbnet.syntax.VnSyntaxType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * VerbNet syntactic frame.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SyntacticFrame {

    private List<FramePhrase> elements = new ArrayList<>();
    private Map<ThematicRoleType, FramePhrase> roles = new HashMap<>();
    private ListMultimap<VerbNetSyntaxType, FramePhrase> typeMap = LinkedListMultimap.create();
    private VnFrame frame;

    public static SyntacticFrame of(@NonNull VnFrame frame) {
        SyntacticFrame result = new SyntacticFrame();
        result.frame = frame;

        result.addElements(frame.syntax());

        int index = 0;
        for (FramePhrase phrase : result.elements) {
            phrase.index(index++);
            result.typeMap.put(phrase.type(), phrase);
        }

        return result;
    }

    public Optional<FramePhrase> role(@NonNull ThematicRoleType roleType) {
        return Optional.ofNullable(roles.get(roleType));
    }

    public List<FramePhrase> phrases(@NonNull VerbNetSyntaxType type) {
        return typeMap.get(type);
    }

    private void addElements(List<VnSyntax> descList) {
        Optional<Preposition> preposition = Optional.empty();
        for (VnSyntax syntaxElement : descList) {
            FramePhrase element = FramePhrase.of(syntaxElement);

            preposition.ifPresent(prep -> {
                if (syntaxElement.type() == VnSyntaxType.NP) {
                    ((NounPhrase) element).preposition(prep);
                } else {
                    elements.add(prep);
                }
            });

            preposition = Optional.empty();

            if (element instanceof Preposition) {
                preposition = Optional.of((Preposition) element);
            } else {
                if (element instanceof NounPhrase) {
                    this.roles.put(((NounPhrase) element).thematicRoleType(), element);
                }
                elements.add(element);
            }
        }

        preposition.ifPresent(elements::add);
    }


}
