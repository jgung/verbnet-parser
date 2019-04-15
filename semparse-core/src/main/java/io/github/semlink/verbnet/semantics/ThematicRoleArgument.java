package io.github.semlink.verbnet.semantics;

import io.github.semlink.util.StringUtils;
import io.github.semlink.verbnet.type.SemanticArgumentType;
import io.github.semlink.verbnet.type.ThematicRoleType;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Thematic role semantic argument.
 *
 * @author jgung
 */
@Slf4j
@Getter
@Accessors(fluent = true)
public class ThematicRoleArgument<T> extends VariableSemanticArgument<T> {

    public ThematicRoleArgument(@NonNull String value) {
        super(SemanticArgumentType.THEMROLE, value);
        Optional<ThematicRoleType> type = ThematicRoleType.fromString(value);
        if (!type.isPresent()) {
            log.warn("Unrecognized thematic role: {}", value);
            type = Optional.of(ThematicRoleType.NONE);
        }
        this.thematicRoleType = type.get();
    }

    private ThematicRoleType thematicRoleType;

    @Override
    public String toString() {
        return StringUtils.capitalized(thematicRoleType) + "(" + (variable == null ? "?" : variable.toString()) + ")";
    }
}
