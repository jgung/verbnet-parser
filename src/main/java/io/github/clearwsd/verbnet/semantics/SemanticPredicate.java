package io.github.clearwsd.verbnet.semantics;

import java.util.List;
import java.util.stream.Collectors;

import edu.mit.jverbnet.data.ThematicRoleType;
import edu.mit.jverbnet.data.semantics.ArgTypeConstant;
import edu.mit.jverbnet.data.semantics.ArgTypeEvent;
import edu.mit.jverbnet.data.semantics.ArgTypeVerbSpecific;
import edu.mit.jverbnet.data.semantics.IPredicateDesc;
import edu.mit.jverbnet.data.semantics.ISemanticArgType;
import io.github.clearwsd.verbnet.type.SemanticArgumentType;
import io.github.clearwsd.verbnet.type.SemanticPredicateType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static io.github.clearwsd.util.StringUtils.capitalized;

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

    public static SemanticPredicate of(@NonNull IPredicateDesc desc) {
        SemanticPredicateType type = SemanticPredicateType.fromString(desc.getValue().getID());
        List<SemanticArgument> arguments = desc.getArgumentTypes().stream()
                .map(SemanticPredicate::of)
                .collect(Collectors.toList());

        return new SemanticPredicate(type, arguments, desc.getBool());
    }

    public static SemanticArgument of(@NonNull ISemanticArgType argType) {
        SemanticArgumentType type = SemanticArgumentType.fromString(argType.getArgType().getID());

        switch (type) {
            case CONSTANT:
                ArgTypeConstant constant = (ArgTypeConstant) argType;
                return new ConstantArgument(constant.getID());
            case EVENT:
                ArgTypeEvent event = (ArgTypeEvent) argType;
                return new EventArgument(event.name());
            case THEMROLE:
                ThematicRoleType roleType = (ThematicRoleType) argType;
                return new ThematicRoleArgument<>(roleType.getID());
            default:
            case VERBSPECIFIC:
                ArgTypeVerbSpecific vsp = (ArgTypeVerbSpecific) argType;
                return new VerbSpecificArgument<>(vsp.getID());
        }
    }

    @Override
    public String toString() {
        return (!polarity ? "!" : "") + capitalized(type) + "["
                + arguments.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }
}
