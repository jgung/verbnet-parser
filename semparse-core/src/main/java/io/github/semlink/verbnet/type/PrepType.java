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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Preposition type within a VerbNet syntactic frame.
 *
 * @author jgung
 */
@Slf4j
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
    DOWN,
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
    PAST,
    REGARDING,
    RESPECTING,
    THOUGH,
    THROUGH,
    TO,
    TOWARDS,
    UNDER,
    UNTIL,
    UP,
    UPON,
    WITH,
    WITHIN,
    UNKNOWN;

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

    public static EnumSet<PrepType> to() {
        return EnumSet.of(INTO, TO, ONTO);
    }

    public static PrepType fromString(@NonNull String string) {
        try {
            return PrepType.valueOf(string.trim().toUpperCase());
        } catch (Exception ignored) {
            return UNKNOWN;
        }
    }
}