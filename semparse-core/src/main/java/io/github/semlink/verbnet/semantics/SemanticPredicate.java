package io.github.semlink.verbnet.semantics;

import io.github.clearwsd.verbnet.semantics.VnPredicatePolarity;
import io.github.clearwsd.verbnet.semantics.VnSemanticArgument;
import io.github.clearwsd.verbnet.semantics.VnSemanticPredicate;
import io.github.semlink.util.StringUtils;
import io.github.semlink.verbnet.type.SemanticArgumentType;
import io.github.semlink.verbnet.type.SemanticPredicateType;
import java.util.List;
import java.util.stream.Collectors;
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

    private SemanticPredicateType type;
    private List<SemanticArgument> arguments;
    private boolean polarity;

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

        return new SemanticPredicate(type, arguments, desc.polarity() == VnPredicatePolarity.TRUE);
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

    @Override
    public String toString() {
        return (!polarity ? "!" : "") + StringUtils.capitalized(type) + "["
            + arguments.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }
}
