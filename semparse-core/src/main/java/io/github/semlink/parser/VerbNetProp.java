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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.semlink.verbnet.VnClass;
import io.github.semlink.app.Span;
import io.github.semlink.propbank.type.FunctionTag;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.verbnet.semantics.Event;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.NonNull;

/**
 * VerbNet proposition with associated semantic predicates and PropBank modifier arguments.
 *
 * @author jamesgung
 */
public interface VerbNetProp {

    /**
     * Return the {@link VnClass} for this proposition.
     */
    VnClass vncls();

    /**
     * Ordered list of {@link Event events} for this proposition, each with associated semantic representations.
     */
    List<Event> subEvents();

    /**
     * Provides semantic representations associated with the main event.
     */
    Optional<Event> mainEvent();

    /**
     * Return thematic roles organized by type.
     */
    Map<ThematicRoleType, List<Span<ThematicRoleType>>> rolesByType();

    /**
     * Return PropBank modifiers (ArgMs) that are not mapped to a thematic role.
     */
    Map<FunctionTag, List<Span<PropBankArg>>> modifiersByType();

    /**
     * Return all thematic roles for a given type.
     */
    default List<Span<ThematicRoleType>> rolesByType(@NonNull ThematicRoleType type) {
        return rolesByType().getOrDefault(type, Collections.emptyList());
    }

    /**
     * Return all PropBank modifiers for a given modifier type.
     */
    default List<Span<PropBankArg>> modifiersByType(@NonNull FunctionTag type) {
        return modifiersByType().getOrDefault(type, Collections.emptyList());
    }

}
