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