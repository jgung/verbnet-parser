package io.github.semlink.verbnet.type;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum NounPhraseSynRelType {

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

    public static NounPhraseSynRelType fromString(@NonNull String string) {
        try {
            return valueOf(string.toUpperCase().trim());
        } catch (Exception ignored) {
            log.warn("Unrecognized NP syntactic restriction: {}", string);
            return UNKNOWN;
        }
    }

}
