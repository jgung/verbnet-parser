package io.github.clearwsd.semlink;

import com.google.common.collect.ImmutableList;
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
import io.github.clearwsd.verbnet.VerbNetClass;
import io.github.clearwsd.verbnet.type.SyntacticFrame;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * PropBank VerbNet role mapping service.
 *
 * @author jgung
 */
public class PropBankVerbNetAligner {

    private PbVnMappings mappings;

    private List<PbVnAligner> aligners = ImmutableList.of(
            new RelAligner(),
            new RoleMappingAligner(),
            new SynResAligner(),
            new SelResAligner(),
            new FillerAligner(),
            new SelResAligner(SelResAligner::getThematicRolesGreedy)
    );

    public PropBankVerbNetAligner(@NonNull PbVnMappings mappings) {
        this.mappings = mappings;
    }

    private PbVnAlignment align(@NonNull Proposition<VerbNetClass, PropBankArg> proposition,
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

        String lemma = source.get(prop.relSpan().startIndex()).feature(FeatureType.Lemma);
        List<Roleset> rolesets = prop.predicate().sense().relatedClasses().stream()
            .map(s -> mappings.rolesets(lemma, s.id().classId()))
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

        // enumerate VerbNet frames
        for (VerbNetClass cls : prop.predicate().sense().relatedClasses()) {

            // iterate over individual frames
            for (IFrame frame : cls.verbClass().getFrames()) {

                SyntacticFrame syntacticFrame = SyntacticFrame.of(frame);

                PbVnAlignment align = align(prop, phrases, syntacticFrame, rolesets);
                alignments.add(align);

            }

        }

        if (alignments.size() > 0) {
            return Optional.of(Collections.max(alignments, alignmentComparator()));
        }
        return Optional.empty();
    }

    private static Comparator<PbVnAlignment> alignmentComparator() {
        Comparator<PbVnAlignment> comparing = Comparator.comparing(al
                -> al.sourcePhrases(true).size());
        comparing.thenComparing(al -> al.targetPhrases().size() - al.targetPhrases(false).size());
        return comparing;
    }

    public static PropBankVerbNetAligner of(@NonNull String mappingsPath) {
        try {
            PbVnMappings mappings = new PbVnMappings(PbVnMapping.fromJson(new FileInputStream(mappingsPath)));
            return new PropBankVerbNetAligner(mappings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
