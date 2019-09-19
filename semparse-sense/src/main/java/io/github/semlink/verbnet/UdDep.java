package io.github.semlink.verbnet;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Universal dependencies dependency labels.
 *
 * @author jamesgung
 */
@Slf4j
public enum UdDep {

    NSUBJ(UdDepType.CORE, UdDepType.NOMINAL),
    NSUBJPASS(UdDepType.CORE, UdDepType.NOMINAL),

    OBJ(UdDepType.CORE, UdDepType.NOMINAL),
    IOBJ(UdDepType.CORE, UdDepType.NOMINAL),
    CSUBJ(UdDepType.CORE, UdDepType.CLAUSE),
    CSUBJPASS(UdDepType.CORE, UdDepType.CLAUSE),
    CCOMP(UdDepType.CORE, UdDepType.CLAUSE),
    XCOMP(UdDepType.CORE, UdDepType.CLAUSE),

    OBL(UdDepType.NON_CORE_DEP, UdDepType.NOMINAL),
    VOCATIVE(UdDepType.NON_CORE_DEP, UdDepType.NOMINAL),
    EXPL(UdDepType.NON_CORE_DEP, UdDepType.NOMINAL),
    DISLOCATED(UdDepType.NON_CORE_DEP, UdDepType.NOMINAL),
    ADVCL(UdDepType.NON_CORE_DEP, UdDepType.CLAUSE),
    ADVMOD(UdDepType.NON_CORE_DEP, UdDepType.MODIFIER_WORD),
    DISCOURSE(UdDepType.NON_CORE_DEP, UdDepType.MODIFIER_WORD),
    AUX(UdDepType.NON_CORE_DEP, UdDepType.FUNCTION_WORD),
    COP(UdDepType.NON_CORE_DEP, UdDepType.FUNCTION_WORD),
    MARK(UdDepType.NON_CORE_DEP, UdDepType.FUNCTION_WORD),

    NMOD(UdDepType.NOMINAL_DEP, UdDepType.NOMINAL),
    NMODNPMOD(UdDepType.NOMINAL_DEP, UdDepType.NOMINAL),
    NMODTMOD(UdDepType.NOMINAL_DEP, UdDepType.NOMINAL),
    NMODPOSS(UdDepType.NOMINAL_DEP, UdDepType.NOMINAL),

    APPOS(UdDepType.NOMINAL_DEP, UdDepType.NOMINAL),
    NUMMOD(UdDepType.NOMINAL_DEP, UdDepType.NOMINAL),
    ACL(UdDepType.NOMINAL_DEP, UdDepType.CLAUSE),
    ACLRELCL(UdDepType.NOMINAL_DEP, UdDepType.CLAUSE),
    AMOD(UdDepType.NOMINAL_DEP, UdDepType.MODIFIER_WORD),
    DET(UdDepType.NOMINAL_DEP, UdDepType.FUNCTION_WORD),
    DETPREDET(UdDepType.NOMINAL_DEP, UdDepType.FUNCTION_WORD),
    CLF(UdDepType.NOMINAL_DEP, UdDepType.FUNCTION_WORD),
    CASE(UdDepType.NOMINAL_DEP, UdDepType.FUNCTION_WORD),

    CONJ(UdDepType.COORDINATION),
    CC(UdDepType.COORDINATION),
    CCPRECONJ(UdDepType.COORDINATION),

    FIXED(UdDepType.MWE),
    FLAT(UdDepType.MWE),
    COMPOUND(UdDepType.MWE),
    COMPOUNDPRT(UdDepType.MWE),

    LIST(UdDepType.LOOSE),
    PARATAXIS(UdDepType.LOOSE),

    ORPHAN(UdDepType.SPECIAL),
    GOESWITH(UdDepType.SPECIAL),
    REPARANDUM(UdDepType.SPECIAL),

    PUNCT(UdDepType.OTHER),
    ROOT(UdDepType.OTHER),
    DEP(UdDepType.OTHER);

    @Getter
    @Accessors(fluent = true)
    private Set<UdDepType> categories;

    UdDep(UdDepType... categories) {
        this.categories = Arrays.stream(categories).collect(Collectors.toSet());
    }

    public static UdDep of(@NonNull String string) {
        try {
            return UdDep.valueOf(string.toUpperCase().replaceAll(":", ""));
        } catch (IllegalArgumentException e) {
            log.debug("Unexpected dependency label: {}", string);
            return UdDep.DEP;
        }
    }

    boolean nominal() {
        return categories.contains(UdDepType.NOMINAL);
    }

    boolean clause() {
        return categories.contains(UdDepType.CLAUSE);
    }

}
