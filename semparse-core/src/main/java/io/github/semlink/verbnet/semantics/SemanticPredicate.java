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

package io.github.semlink.verbnet.semantics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.semlink.util.StringUtils;
import io.github.semlink.verbnet.type.SemanticArgumentType;
import io.github.semlink.verbnet.type.SemanticPredicateType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * VerbNet semantic predicate.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
@EqualsAndHashCode
public class SemanticPredicate {

    public static final String DEFAULT_EVENT = "E";

    private SemanticPredicateType type;
    private List<SemanticArgument> arguments;
    private boolean polarity;

    public EventArgument event() {
        return arguments.stream()
                .filter(arg -> arg.type == SemanticArgumentType.EVENT)
                .map(arg -> (EventArgument) arg).min(Comparator.comparing(EventArgument::index)).orElse(new EventArgument(DEFAULT_EVENT));
    }

    public <T> List<T> get(@NonNull SemanticArgumentType type) {
        //noinspection unchecked
        return (List<T>) arguments.stream()
                .filter(i -> i.type() == type)
                .collect(Collectors.toList());
    }

    public static SemanticPredicate of(@NonNull VnSemanticPredicate desc) {
        SemanticPredicateType type = SemanticPredicateType.fromString(desc.type());
        List<SemanticArgument> arguments = desc.semanticArguments().stream()
                .map(SemanticPredicate::of)
                .collect(Collectors.toList());

        List<SemanticArgument> result = new ArrayList<>();
        if (arguments.stream().filter(arg -> arg.type == SemanticArgumentType.EVENT).count() > 1) {
            result.add(new EventArgument<>(DEFAULT_EVENT));
        }
        result.addAll(arguments);

        return new SemanticPredicate(type, result, desc.polarity() == VnPredicatePolarity.TRUE);
    }

    public static SemanticArgument of(@NonNull VnSemanticArgument argType) {
        SemanticArgumentType type = SemanticArgumentType.fromString(argType.type());

        switch (type) {
            case CONSTANT:
                return new ConstantArgument(argType.value());
            case EVENT:
                return new EventArgument(argType.value());
            case THEMROLE:
                return new ThematicRoleArgument<>(argType.value());
            default:
            case VERBSPECIFIC:
                return new VerbSpecificArgument<>(argType.value());
        }
    }

    public Optional<String> description() {
        if (type == SemanticPredicateType.CAUSE) {
            List<EventArgument> args = arguments.stream().filter(a -> a.type == SemanticArgumentType.EVENT)
                    .map(a -> (EventArgument) a)
                    .filter(a -> !a.mainEvent())
                    .collect(Collectors.toList());
            if (args.size() == 2) {
                return Optional.of(String.format("%s causes %s", args.get(0).id(), args.get(1).id()));
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return (!polarity ? "!" : "") + StringUtils.capitalized(type) + "["
                + arguments.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }
}
