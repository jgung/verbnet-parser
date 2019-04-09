package io.github.semlink.verbnet.type;

import lombok.NonNull;

/**
 * Semantic argument types.
 *
 * @author jgung
 */
public enum SemanticArgumentType {

    CONSTANT,
    EVENT,
    THEMROLE,
    VERBSPECIFIC;

    public static SemanticArgumentType fromString(@NonNull String string) {
        return valueOf(string.trim().toUpperCase());
    }

}
