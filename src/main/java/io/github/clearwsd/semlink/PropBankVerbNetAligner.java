package io.github.clearwsd.semlink;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import edu.mit.jverbnet.data.IFrame;
import io.github.clearwsd.parser.Proposition;
import io.github.clearwsd.propbank.type.PropBankArg;
import io.github.clearwsd.semlink.PbVnMappings.Roleset;
import io.github.clearwsd.semlink.aligner.FillerAligner;
import io.github.clearwsd.semlink.aligner.PbVnAligner;
import io.github.clearwsd.semlink.aligner.PbVnAlignment;
import io.github.clearwsd.semlink.aligner.RelAligner;
import io.github.clearwsd.semlink.aligner.RoleMappingAligner;
import io.github.clearwsd.semlink.aligner.SelResAligner;
import io.github.clearwsd.semlink.aligner.SynResAligner;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.verbnet.VerbNet;
import io.github.clearwsd.verbnet.VerbNetClass;
import io.github.clearwsd.verbnet.type.SyntacticFrame;
import lombok.NonNull;

/**
 * PropBank VerbNet role mapping service.
 *
 * @author jgung
 */
public class PropBankVerbNetAligner {

    private VerbNet verbNet;
    private PbVnMappings mappings;

    private List<PbVnAligner> aligners = ImmutableList.of(
            new RelAligner(),
            new RoleMappingAligner(),
            new SynResAligner(),
            new SelResAligner(),
            new FillerAligner()
    );

    public PropBankVerbNetAligner(@NonNull VerbNet verbNet, @NonNull PbVnMappings mappings) {
        this.verbNet = verbNet;
        this.mappings = mappings;
    }

    public PbVnAlignment align(@NonNull Proposition<VerbNetClass, PropBankArg> proposition,
                               @NonNull List<PropBankPhrase> chunk,
                               @NonNull SyntacticFrame frame,
                               @NonNull List<Roleset> rolesets) {

        PbVnAlignment pbVnAlignment = new PbVnAlignment()
                .alignment(Alignment.of(chunk, frame.elements()))
                .frame(frame)
                .propbankPhrases(chunk)
                .rolesets(rolesets)
                .proposition(proposition);

        for (PbVnAligner aligner : aligners) {
            aligner.align(pbVnAlignment);
        }

        return pbVnAlignment;
    }

    public Optional<PbVnAlignment> align(@NonNull Proposition<VerbNetClass, PropBankArg> prop,
                                         @NonNull DepTree source) {

        List<PropBankPhrase> phrases = PropBankPhrase.fromProp(prop, source);

        List<PbVnAlignment> alignments = new ArrayList<>();
        // enumerate VerbNet frames
        for (VerbNetClass cls : prop.predicate().sense().subClasses()) {

            // iterate over individual frames
            for (IFrame frame : cls.verbClass().getFrames()) {

                String lemma = source.get(prop.relSpan().startIndex()).feature(FeatureType.Lemma);
                SyntacticFrame syntacticFrame = SyntacticFrame.of(frame);

                List<Roleset> rolesets = mappings.rolesets(lemma, cls.id().classId());
                PbVnAlignment align = align(prop, phrases, syntacticFrame, rolesets);
                alignments.add(align);

            }

        }

        if (alignments.size() > 0) {
            return Optional.of(Collections.max(alignments, alignmentComparator()));
        }
        return Optional.empty();
    }

    public static Comparator<PbVnAlignment> alignmentComparator() {
        Comparator<PbVnAlignment> comparing = Comparator.comparing(al
                -> al.sourcePhrases(true).size());
        comparing.thenComparing(al -> al.targetPhrases().size() - al.targetPhrases(false).size());
        return comparing;
    }

}
