package io.github.clearwsd.semlink.aligner;

import io.github.clearwsd.propbank.type.ArgNumber;
import io.github.clearwsd.semlink.PropBankPhrase;
import io.github.clearwsd.verbnet.type.NounPhrase;
import io.github.clearwsd.verbnet.type.ThematicRoleType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * Just map remaining numbered args to any remaining valid frame roles.
 *
 * @author jgung
 */
public class FillerAligner implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {
        for (PropBankPhrase phrase : alignment.sourcePhrases(false)) {
            List<NounPhrase> collect = alignment.targetPhrases(false).stream()
                    .filter(i -> i instanceof NounPhrase)
                    .map(i -> ((NounPhrase) i))
                    .collect(Collectors.toList());
            if (phrase.getNumber() == ArgNumber.A0) {
                for (NounPhrase unalignedPhrase : collect) {
                    if (unalignedPhrase.thematicRoleType() == ThematicRoleType.AGENT) {
                        alignment.add(phrase, unalignedPhrase);
                        break;
                    }
                }
            }
            if (phrase.getNumber() == ArgNumber.A1) {
                for (NounPhrase unalignedPhrase : collect) {
                    if (unalignedPhrase.thematicRoleType() == ThematicRoleType.THEME
                        || unalignedPhrase.thematicRoleType() == ThematicRoleType.PATIENT) {
                        alignment.add(phrase, unalignedPhrase);
                        break;
                    }
                }
            } else if (phrase.getNumber() == ArgNumber.A4) {
                for (NounPhrase unalignedPhrase : collect) {
                    if (unalignedPhrase.thematicRoleType() == ThematicRoleType.DESTINATION) {
                        alignment.add(phrase, unalignedPhrase);
                        break;
                    }
                }
            }


        }
    }

}
