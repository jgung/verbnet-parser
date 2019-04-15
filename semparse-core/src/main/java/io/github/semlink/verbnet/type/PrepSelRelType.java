package io.github.semlink.verbnet.type;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public enum PrepSelRelType {

    DEST_DIR,
    DEST_CONF,
    PATH,
    SRC,
    LOC,
    DIR,
    SPATIAL,
    UNKNOWN;

    public static PrepSelRelType fromString(@NonNull String string) {
        try {
            return valueOf(string.toUpperCase().trim());
        } catch (Exception ignored) {
        }
        log.warn("Unrecognized preposition selectional restriction: {}", string);
        return UNKNOWN;
    }
}