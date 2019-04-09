package io.github.semlink.semlink.aligner;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.stream.Collectors;

import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.verbnet.type.FramePhrase;
import io.github.semlink.verbnet.type.VerbNetSyntaxType;
import lombok.NonNull;

/**
 * Aligns verb/PropBank rel.
 *
 * @author jgung
 */
public class RelAligner implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {
        List<PropBankPhrase> rels = alignment.byNumber(ArgNumber.V).stream()
                .filter(rel -> !rel.isContinuation())
                .filter(rel -> !rel.isReference()).collect(Collectors.toList());
        List<FramePhrase> verbs = alignment.bySyntacticType(VerbNetSyntaxType.VERB);
        Preconditions.checkState(rels.size() == 1 && verbs.size() == 1);
        alignment.add(rels.get(0), verbs.get(0));
    }

}
