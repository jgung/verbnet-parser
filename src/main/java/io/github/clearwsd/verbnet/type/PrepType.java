package io.github.clearwsd.verbnet.type;

import java.util.HashSet;
import java.util.Set;

import lombok.NonNull;

public enum PrepType {
    ABOUT,
    AFTER,
    AGAINST,
    AMONG,
    AS,
    AT,
    BEFORE,
    BETWEEN,
    BY,
    CONCERNING,
    FOR,
    FROM,
    IF,
    IN,
    IN_BETWEEN,
    INTO,
    LIKE,
    OF,
    OFF,
    ON,
    ONTO,
    OUT_OF,
    OVER,
    REGARDING,
    RESPECTING,
    THOUGH,
    THROUGH,
    TO,
    TOWARDS,
    UNDER,
    UNTIL,
    UPON,
    WITH;

    public static Set<PrepType> fromString(@NonNull String string) {
        String[] fields = string.trim().toUpperCase().replaceAll("\\?", "").split("\\||\\s+");
        Set<PrepType> values = new HashSet<>();
        for (String val : fields) {
            try {
                values.add(PrepType.valueOf(val.trim()));
            } catch (Exception ignored) {
            }
        }
        return values;
    }
}