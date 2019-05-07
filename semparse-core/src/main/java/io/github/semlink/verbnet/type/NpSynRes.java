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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Syntactic restriction for a Noun Phrase in a VerbNet syntactic frame.
 *
 * @author jgung
 */
@Slf4j
public enum NpSynRes {

    AC_ING,
    AC_TO_INF,
    ADV_LOC,
    BE_SC_ING,
    DEFINITE,
    FOR_COMP,
    GENITIVE,
    HOW_EXTRACT,
    NP_ING,
    NP_OMIT_ING,
    NP_P_ING,
    NP_PPART,
    NP_TO_INF,
    OC_BARE_INF,
    OC_ING,
    OC_TO_INF,
    PLURAL,
    POSS_ING,
    QUOTATION,
    REFL,
    RS_TO_INF,
    SC_ING,
    SC_TO_INF,
    SENTENTIAL,
    SMALL_CLAUSE,
    TENSED_THAT,
    THAT_COMP,
    TO_BE,
    WH_COMP,
    WH_EXTRACT,
    WH_INF,
    WH_ING,
    WHAT_EXTRACT,
    WHAT_INF,
    WHETH_INF,
    UNKNOWN;

    public static NpSynRes fromString(@NonNull String string) {
        try {
            return valueOf(string.toUpperCase().trim());
        } catch (Exception ignored) {
            log.warn("Unrecognized NP syntactic restriction: {}", string);
            return UNKNOWN;
        }
    }

}
