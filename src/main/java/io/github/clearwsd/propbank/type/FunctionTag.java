package io.github.clearwsd.propbank.type;

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