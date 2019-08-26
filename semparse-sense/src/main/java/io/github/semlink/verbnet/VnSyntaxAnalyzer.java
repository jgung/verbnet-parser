package io.github.semlink.verbnet;

import com.google.common.collect.ImmutableSet;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.verbnet.type.NpSynRes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * VerbNet syntax analyzer.
 *
 * @author jamesgung
 */
public class VnSyntaxAnalyzer {

    private static final Set<String> PLURAL_PRONOUNS = ImmutableSet.of("you", "us", "them", "we", "they",
        "themselves", "themself", "yourselves", "ourselves");

    public static boolean definite(DepNode depNode) {
        if (depNode.children().stream().anyMatch(c -> c.dep().equalsIgnoreCase("det")
            && ImmutableSet.of("the", "this", "that", "these", "those", "them")
            .contains(c.feature(FeatureType.Lemma).toString()))) {
            return true;
        }
        if (isPronoun(depNode)) {
            return true;
        }
        if (isProperNoun(depNode)) {
            return true;
        }
        return false;
    }

    public static boolean isWh(DepNode depNode) {
        return matchesLemmaPosDep(depNode, "if", "IN", "mark")
            || matchesPos(depNode, "WP")
            || matchesPos(depNode, "WRB")
            || matchesLemmaPosDep(depNode, "whether", "IN", "mark");
    }

    public static boolean isReflexive(DepNode node) {
        return matchesPos(node, "PRP") && node.feature(FeatureType.Text).toString().endsWith("self");
    }

    public static boolean isClause(DepNode node) {
        return ImmutableSet.of("csubj", "ccomp", "xcomp", "advcl", "acl").contains(node.dep())
            || node.feature(FeatureType.Pos).toString().toUpperCase().startsWith("V") && node.dep().equalsIgnoreCase("dep");
    }

    public static boolean isNoun(DepNode node) {
        return node.feature(FeatureType.Pos).toString().toUpperCase().startsWith("N");
    }

    public static boolean isProperNoun(DepNode node) {
        String pos = node.feature(FeatureType.Pos).toString().toUpperCase();
        return pos.equalsIgnoreCase("NNP") || pos.equalsIgnoreCase("NNPS");
    }

    public static boolean isPronoun(DepNode node) {
        String pos = node.feature(FeatureType.Pos).toString().toUpperCase();
        return pos.equalsIgnoreCase("PRP") || pos.equalsIgnoreCase("WP");
    }

    public static boolean isPlural(DepNode node) {
        return matchesPos(node, "NNS") || matchesPos(node, "NNPS")
            || PLURAL_PRONOUNS.contains(node.feature(FeatureType.Text).toString().toLowerCase());
    }

    public static boolean matchesLemmaPos(DepNode node, String lemma, String pos) {
        return node.feature(FeatureType.Lemma).toString().equalsIgnoreCase(lemma) && matchesPos(node, pos);
    }

    public static boolean matchesPosDep(DepNode node, String pos, String dep) {
        return node.dep().equalsIgnoreCase(dep) && matchesPos(node, pos);
    }

    public static boolean matchesPos(DepNode node, String pos) {
        return node.feature(FeatureType.Pos).toString().equalsIgnoreCase(pos);
    }

    public static boolean matchesLemmaPosDep(DepNode node, String lemma, String pos, String dep) {
        return node.feature(FeatureType.Lemma).toString().equalsIgnoreCase(lemma)
            && matchesPos(node, pos) && node.dep().equalsIgnoreCase(dep);
    }

    public static boolean adjectiveOrPastParticiple(DepNode node) {
        return matchesPos(node, "VBN") || matchesPosDep(node, "JJ", "xcomp");
    }

    public static boolean hasSubject(DepNode node) {
        return node.children().stream().anyMatch(c -> c.dep().equalsIgnoreCase("nsubj"));
    }


    public Set<Restriction<NpSynRes>> analyzeNp(DepNode argument) {
        if (argument.isRoot()) {
            return new HashSet<>();
        }

        Set<Restriction<NpSynRes>> restrictions = new HashSet<>();

        final String argDep = argument.dep();
        final List<DepNode> children = argument.children();

        if (isClause(argument) && children.stream().anyMatch(c -> matchesLemmaPosDep(c, "to", "TO", "mark"))
            && children.stream().anyMatch(c -> matchesLemmaPosDep(c, "be", "VB", "cop"))
            && argument.head().children().stream().anyMatch(c -> c.dep().equalsIgnoreCase("obj"))) {
            // I wished [him to be a good doctor]
            restrictions.add(Restriction.of(true, NpSynRes.TO_BE));
        }

        if (argDep.equalsIgnoreCase("ccomp")
            && children.stream().anyMatch(c -> matchesLemmaPosDep(c, "that", "IN", "mark"))) {
            // I wished [that she would come immediately]
            restrictions.add(Restriction.of(true, NpSynRes.THAT_COMP));
            if (matchesPos(argument, "VB")) {
                restrictions.add(Restriction.of(false, NpSynRes.TENSED_THAT));
            } else {
                restrictions.add(Restriction.of(true, NpSynRes.TENSED_THAT));
            }
        }

        if (argDep.equalsIgnoreCase("xcomp")
            && children.stream().anyMatch(c -> matchesLemmaPosDep(c, "to", "TO", "mark"))) {
            // I wished [to move up the corporate ladder]
            restrictions.add(Restriction.of(true, NpSynRes.SC_TO_INF));
            restrictions.add(Restriction.of(true, NpSynRes.AC_TO_INF));

            // I needed him to go, She relies on him to help, Success requires everyone to work hard
            restrictions.add(Restriction.of(true, NpSynRes.NP_TO_INF));
            // He appeared to leave (raising subject)
            restrictions.add(Restriction.of(true, NpSynRes.RS_TO_INF));
        }

        if (matchesPosDep(argument, "VBG", "xcomp") || matchesPosDep(argument, "VBG", "advcl")) {
            // He described [going to work], I helped [with finishing the homework]
            restrictions.add(Restriction.of(true, NpSynRes.AC_ING));
            // John professed [loving the miscreants]
            restrictions.add(Restriction.of(true, NpSynRes.BE_SC_ING));
            // He backed out of going on the trip
            restrictions.add(Restriction.of(true, NpSynRes.SC_ING));
            if (argDep.equalsIgnoreCase("xcomp")) {
                // I need [exercising]
                restrictions.add(Restriction.of(true, NpSynRes.NP_OMIT_ING));
            }
        }

        if (matchesPosDep(argument, "RB", "advmod")) {
            //  We camped there
            restrictions.add(Restriction.of(true, NpSynRes.ADV_LOC));
        }

        if (isNoun(argument)) {
            if (definite(argument)) {
                restrictions.add(Restriction.of(true, NpSynRes.DEFINITE));
            } else {
                restrictions.add(Restriction.of(false, NpSynRes.DEFINITE));
            }
        }

        if (isClause(argument) && children.stream().anyMatch(c -> matchesLemmaPosDep(c, "for", "IN", "mark"))) {
            // I wished [for John to leave]
            restrictions.add(Restriction.of(true, NpSynRes.FOR_COMP));
        }

        if (argument.children().stream().anyMatch(c -> c.dep().equalsIgnoreCase("nmod:poss"))) {
            // TODO: break up this argument into possessor + attribute
            restrictions.add(Restriction.of(true, NpSynRes.GENITIVE));
        }

        if (argDep.equalsIgnoreCase("ccomp") && argument.children().stream()
            .anyMatch(c -> matchesLemmaPos(c, "how", "WRB"))) {
            restrictions.add(Restriction.of(true, NpSynRes.HOW_EXTRACT));
        }

        if (matchesPos(argument, "VBG") && hasSubject(argument)) {
            // I discovered about him drinking
            restrictions.add(Restriction.of(true, NpSynRes.NP_ING));
        }

        if (argDep.equalsIgnoreCase("advcl") && argument.children().stream()
            .anyMatch(c -> matchesLemmaPosDep(c, "be", "VBG", "cop"))) {
            // They considered him [as being stupid]
            restrictions.add(Restriction.of(true, NpSynRes.NP_P_ING));
        }

        if (adjectiveOrPastParticiple(argument) && hasSubject(argument)) {
            // They considered the matter closed
            // He declared the patient dead
            restrictions.add(Restriction.of(true, NpSynRes.NP_PPART));
        }

        if (matchesPosDep(argument, "VB", "ccomp")) {
            // I watched her bake the cake
            restrictions.add(Restriction.of(true, NpSynRes.OC_BARE_INF));
        }

        if ((matchesPosDep(argument, "VBG", "dep") || matchesPosDep(argument, "VBG", "ccomp"))
            && hasSubject(argument)) {
            // I saw her crying
            restrictions.add(Restriction.of(true, NpSynRes.OC_ING));
        }

        if ((argDep.equalsIgnoreCase("xcomp") || argDep.equalsIgnoreCase("advcl"))
            && children.stream().anyMatch(c -> matchesLemmaPosDep(c, "to", "TO", "mark"))) {
            // The Senate ordered him to disband his army .
            restrictions.add(Restriction.of(true, NpSynRes.OC_TO_INF));
        }

        if (isPlural(argument) || ((isPronoun(argument) || isNoun(argument)) && argument.children().stream()
            .anyMatch(c -> c.dep().equalsIgnoreCase("conj")))) {
            // [John and Mary] married
            // [The grocery carts] thudded together
            //
            restrictions.add(Restriction.of(true, NpSynRes.PLURAL));
        }

        if (argument.children().stream().anyMatch(c -> c.dep().equalsIgnoreCase("nmod:poss"))
            && argument.feature(FeatureType.Text).toString().toLowerCase().endsWith("ing")) {
            // The rules forbid our smoking
            restrictions.add(Restriction.of(true, NpSynRes.POSS_ING));
        }

        if (argument.children().stream().anyMatch(c -> matchesPos(c, "``") || matchesPos(c, "''"))) {
            // Ellen warned, 'Avoid that hole in the sidewalk.'
            restrictions.add(Restriction.of(true, NpSynRes.QUOTATION));
        }

        if (argument.children().stream().anyMatch(c -> matchesPos(c, "``") || matchesPos(c, "''"))) {
            // Ellen warned, 'Avoid that hole in the sidewalk.'
            restrictions.add(Restriction.of(true, NpSynRes.QUOTATION));
        }

        if (isReflexive(argument)) {
            // Tessa hurt herself
            restrictions.add(Restriction.of(true, NpSynRes.REFL));
        }

        if (isClause(argument)) {
            restrictions.add(Restriction.of(true, NpSynRes.SENTENTIAL));
        } else {
            restrictions.add(Restriction.of(false, NpSynRes.SENTENTIAL));
        }

        if (argDep.equalsIgnoreCase("advcl") && argument.children().stream()
            .anyMatch(c -> matchesLemmaPosDep(c, "be", "VB", "cop"))) {
            // They considered him [as being stupid]
            // TODO: not really technically small clause
            restrictions.add(Restriction.of(true, NpSynRes.SMALL_CLAUSE));
        }

        if (isClause(argument) && children.stream().anyMatch(VnSyntaxAnalyzer::isWh)) {

            // He revealed if we should come
            // I understood why we should help them
            // He considered whether he should come
            restrictions.add(Restriction.of(true, NpSynRes.WH_COMP));

            boolean what = children.stream().anyMatch(c -> matchesLemmaPos(c, "what", "WP"));

            if (matchesPos(argument, "VB")
                && children.stream().anyMatch(c -> matchesLemmaPosDep(c, "to", "TO", "mark"))) {
                restrictions.add(Restriction.of(true, NpSynRes.WH_INF));
                restrictions.add(Restriction.of(true, NpSynRes.WH_ING)); // TODO: looks like a VerbNet error
                if (what) {
                    // The president declared what we should do
                    restrictions.add(Restriction.of(true, NpSynRes.WHAT_INF));
                } else if (argument.children().stream().anyMatch(c -> matchesLemmaPosDep(c, "whether", "IN", "mark"))) {
                    // I asked of him about whether to go
                    restrictions.add(Restriction.of(true, NpSynRes.WHETH_INF));
                }
            } else {
                restrictions.add(Restriction.of(true, NpSynRes.WH_EXTRACT));
                if (what) {
                    // I rejected what they were doing
                    // John suggested to her what she could do
                    restrictions.add(Restriction.of(true, NpSynRes.WHAT_EXTRACT));
                }
            }
        }

        return restrictions;
    }

}
