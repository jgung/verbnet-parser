package io.github.semlink.verbnet.type;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import lombok.NonNull;

public enum PrepType {

    ABOUT,
    ABOVE,
    AFTER,
    AGAINST,
    AMONG,
    AS,
    AT,
    BACK,
    BESIDE,
    BEFORE,
    BELOW,
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
    OUT,
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

    public boolean isTrajectory() {
        return EnumSet.of(BETWEEN, IN_BETWEEN, THROUGH, OVER, UNDER, ABOVE, BELOW, BACK, BESIDE).contains(this);
    }

    public boolean maybeLocation() {
        return EnumSet.of(UPON, UNDER, TOWARDS, TO, THROUGH, OVER, OUT_OF, ONTO, ON, OFF, INTO, IN_BETWEEN, IN, FROM,
            BY, BETWEEN, BELOW, BACK, AT, ABOVE).contains(this);
    }

    public boolean maybeSource() {
        return EnumSet.of(OUT_OF, FROM).contains(this);
    }

    public boolean maybeDestination() {
        return EnumSet.of(FOR, TO, INTO, TOWARDS, ONTO, ON, AT).contains(this);
    }

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