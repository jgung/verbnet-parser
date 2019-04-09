package io.github.semlink.verbnet.type;

import java.util.EnumSet;
import java.util.Optional;

import io.github.semlink.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static io.github.semlink.util.StringUtils.capitalized;

@Slf4j
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

    public boolean isStartingPoint() {
        return EnumSet.of(SOURCE, INITIAL_STATE, INITIAL_LOCATION).contains(this);
    }

    public boolean isEndingPoint() {
        return EnumSet.of(GOAL, RESULT, PRODUCT, DESTINATION, FINAL_TIME, RECIPIENT, TRAJECTORY).contains(this);
    }

    public boolean isAgentive() {
        return EnumSet.of(AGENT, CAUSER).contains(this);
    }

    @Override
    public String toString() {
        return StringUtils.capitalized(this);
    }

    public static Optional<ThematicRoleType> fromString(@NonNull String themRole) {
        themRole = themRole.toUpperCase().trim()
            .replaceAll(" ", "_")
            .replaceAll("-", "_")
            .replaceAll("\\?", "");
        try {
            return Optional.of(valueOf(themRole.toUpperCase().trim()));
        } catch (Exception ignored) {
            if (themRole.equalsIgnoreCase("CAUSE")) {
                return Optional.of(CAUSER);
            }
            log.warn("Unrecognized thematic role type: {}", themRole);
        }
        return Optional.empty();
    }
}