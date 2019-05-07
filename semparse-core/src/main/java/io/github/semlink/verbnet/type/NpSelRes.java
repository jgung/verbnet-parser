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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Syntactic restriction for a Noun Phrase in VerbNet syntactic frame.
 *
 * @author jgung
 */
@Slf4j
@AllArgsConstructor
public enum NpSelRes {

    ABSTRACT,
    ANIMAL,
    ANIMATE,
    AT,
    BIOTIC,
    BODY_PART,
    COMESTIBLE,
    COMMUNICATION,
    CONCRETE,
    CURRENCY,
    DEST,
    DEST_CONF,
    DEST_DIR,
    DIR,
    ELONGATED,
    EVENTIVE,
    FORCE,
    GARMENT,
    HUMAN,
    INT_CONTROL,
    LOC,
    LOCATION,
    MACHINE,
    NONRIGID,
    ORGANIZATION,
    PATH,
    PLURAL,
    POINTY,
    QUESTION,
    REFL,
    REGION,
    SOLID,
    SOUND,
    SPATIAL,
    SRC,
    STATE,
    SUBSTANCE,
    TIME,
    VEHICLE,
    VEHICLE_PART,
    UNKNOWN;

    public static NpSelRes fromString(@NonNull String string) {
        try {
            return valueOf(string.toUpperCase().trim());
        } catch (Exception ignored) {
            log.debug("Unrecognized selectional restriction type: {}", string);
            return UNKNOWN;
        }
    }
}