package io.github.semlink.verbnet.type;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public enum PrepSelRelType {

    DEST_DIR,
    DEST_CONF,
    PATH,
    SRC,
    LOC,
    DIR,
    SPATIAL;

    public static Optional<PrepSelRelType> fromString(@NonNull String string) {
        try {
            return Optional.of(valueOf(string.toUpperCase().trim()));
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}