/*
 * Copyright 2019 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.semlink.verbnet.type;

import java.util.EnumSet;
import java.util.Optional;

import io.github.semlink.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * VerbNet thematic role type.
 *
 * @author jgung
 */
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
    CIRCUMSTANCE,
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
            if (themRole.equalsIgnoreCase("PATIENT_I")) {
                return Optional.of(PATIENT);
            }
            if (themRole.equalsIgnoreCase("PATIENT_J")) {
                return Optional.of(CO_PATIENT);
            }
            log.warn("Unrecognized thematic role type: {}", themRole);
        }
        return Optional.empty();
    }
}