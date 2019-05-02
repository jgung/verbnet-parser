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

package io.github.semlink.propbank.type;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * PropBank function tag.
 *
 * @author jgung
 */
@Slf4j
public enum FunctionTag {

    ADJ,
    ADV,
    CAU,
    COM,
    CXN,
    DIR,
    EXT,
    GOL,
    LOC,
    MNR,
    PAG,
    PPT,
    PRD,
    PNC,
    PRP,
    REC,
    TMP,
    VSP,

    MOD,
    NEG,

    DIS,
    PRR,
    DSP;

    public static FunctionTag fromString(@NonNull String string) {
        try {
            return FunctionTag.valueOf(string.toUpperCase());
        } catch (Exception ignored) {
            log.warn("Missing function tag: {}", string);
        }
        return VSP;
    }
}