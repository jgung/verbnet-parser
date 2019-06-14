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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.clearwsd.verbnet.VnClass;
import io.github.semlink.app.Span;
import io.github.semlink.propbank.type.FunctionTag;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.SemlinkRole;
import io.github.semlink.verbnet.semantics.Event;
import io.github.semlink.verbnet.semantics.EventArgument;
import io.github.semlink.verbnet.semantics.SemanticPredicate;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default {@link VerbNetProp} implementation.
 *
 * @author jgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class DefaultVerbNetProp implements VerbNetProp {

    private List<String> tokens;
    private List<SemanticPredicate> predicates = new ArrayList<>();
    private Proposition<VnClass, SemlinkRole> proposition;

    @Override
    public VnClass vncls() {
        return proposition.predicate();
    }

    @Override
    public List<Event> subEvents() {
        ListMultimap<EventArgument, SemanticPredicate> predicatesByEvent = Multimaps.index(predicates, SemanticPredicate::event);
        List<Event> subEvents = new ArrayList<>();
        predicatesByEvent.keySet().stream()
                .filter(e -> !e.mainEvent())
                .sorted(Comparator.comparing(EventArgument::id))
                .forEach(entry -> subEvents.add(new Event(subEvents.size(), entry, predicatesByEvent.get(entry))));
        return subEvents;
    }

    @Override
    public Optional<Event> mainEvent() {
        List<SemanticPredicate> mainEventPredicates = predicates.stream()
                .filter(pred -> pred.event().mainEvent())
                .collect(Collectors.toList());
        if (mainEventPredicates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Event(-1, mainEventPredicates.get(0).event(), mainEventPredicates));
    }

    @Override
    public Map<ThematicRoleType, List<Span<ThematicRoleType>>> rolesByType() {
        Map<ThematicRoleType, List<Span<ThematicRoleType>>> spansByType = new HashMap<>();
        proposition.arguments().spans().stream()
                .filter(span -> span.label().vn().isPresent())
                .forEach(span -> {
                    List<Span<ThematicRoleType>> spans = spansByType
                            .computeIfAbsent(span.label().thematicRoleType(), type -> new ArrayList<>());
                    spans.add(Span.convert(span, span.label().thematicRoleType()));
                });
        return spansByType;
    }

    @Override
    public List<Span<ThematicRoleType>> rolesByType(@NonNull ThematicRoleType type) {
        return byThematicRole(type).stream()
                .map(span -> Span.convert(span, span.label().thematicRoleType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Span<PropBankArg>> modifiersByType(@NonNull FunctionTag type) {
        return proposition.arguments().spans().stream()
                .filter(span -> !span.label().vn().isPresent())
                .filter(span -> span.label().pb().map(pb -> pb.isModifier() && pb.getFunctionTag() == type).orElse(false))
                .map(span -> Span.convert(span, span.label().propBankArg()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<FunctionTag, List<Span<PropBankArg>>> modifiersByType() {
        Map<FunctionTag, List<Span<PropBankArg>>> spansByType = new HashMap<>();
        proposition.arguments().spans().stream()
                .filter(span -> !span.label().vn().isPresent())
                .filter(span -> span.label().pb().map(PropBankArg::isModifier).orElse(false))
                .forEach(span -> {
                    List<Span<PropBankArg>> spans = spansByType
                            .computeIfAbsent(span.label().propBankArg().getFunctionTag(), type -> new ArrayList<>());
                    spans.add(Span.convert(span, span.label().propBankArg()));
                });
        return spansByType;
    }

    /**
     * Returns a list of {@link Span spans} for {@link SemlinkRole} roles, which directly map PropBank roles with corresponding
     * VerbNet thematic roles and descriptions.
     */
    public List<Span<SemlinkRole>> semlinkRoles() {
        return proposition.arguments().spans();
    }

    /**
     * Return {@link SemlinkRole} spans by VerbNet thematic role type.
     *
     * @param type thematic role type
     * @return SemLink roles for a give thematic role type
     */
    public List<Span<SemlinkRole>> byThematicRole(@NonNull ThematicRoleType type) {
        return proposition.arguments().spans().stream()
                .filter(span -> span.label().vn().map(vn -> type == vn).orElse(false))
                .collect(Collectors.toList());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ----------- Thematic Roles -------- \n");
        sb.append(proposition.toString(tokens)).append("\n");
        if (!predicates.isEmpty()) {
            sb.append(" ----------- Semantic Analysis ----- \n");
            predicates.forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }
}
