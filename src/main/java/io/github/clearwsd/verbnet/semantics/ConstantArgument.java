package io.github.clearwsd.verbnet.semantics;

import io.github.clearwsd.verbnet.type.SemanticArgumentType;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Constant-valued semantic argument.
 *
 * @author jgung
 */
@Slf4j
@Getter
@Accessors(fluent = true)
public class ConstantArgument extends SemanticArgument {

    public ConstantArgument(@NonNull String value) {
        super(SemanticArgumentType.CONSTANT, value);
    }

}
