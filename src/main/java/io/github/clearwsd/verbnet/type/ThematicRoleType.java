package io.github.clearwsd.verbnet.type;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import static io.github.clearwsd.util.StringUtils.capitalized;

@AllArgsConstructor
public enum ThematicRoleType {

    AFFECTOR,
    AGENT,
    ASSET,
    ATTRIBUTE,
    AXIS,
    BENEFICIARY,
    CAUSER,
    CO_AGENT,
    CO_PATIENT,
    CO_THEME,
    CONTEXT,
    DESTINATION,
    DURATION,
    EXPERIENCER,
    EXTENT,
    FINAL_TIME,
    GOAL,
    INITIAL_LOCATION,
    INITIAL_STATE,
    INSTRUMENT,
    LOCATION,
    MANNER,
    MATERIAL,
    PATH,
    PATIENT,
    PIVOT,
    PRECONDITION,
    PREDICATE,
    PRODUCT,
    RECIPIENT,
    REFLEXIVE,
    RESULT,
    SOURCE,
    STIMULUS,
    THEME,
    TIME,
    TOPIC,
    TRAJECTORY,
    VALUE,
    NONE,
    VERB,
    DIRECTION;

    @Override
    public String toString() {
        return capitalized(this);
    }

    public static Optional<ThematicRoleType> fromString(@NonNull String themRole) {
        themRole = themRole.toUpperCase().trim()
                .replaceAll("-", "_")
                .replaceAll("\\?", "");
        try {
            return Optional.of(valueOf(themRole.toUpperCase().trim()));
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}