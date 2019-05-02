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

package io.github.semlink.semlink;

import com.google.common.collect.ImmutableList;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnFrame;
import io.github.semlink.parser.Proposition;
import io.github.semlink.propbank.DefaultPbIndex;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.PbVnMappings.MappedRoleset;
import io.github.semlink.semlink.aligner.FillerAligner;
import io.github.semlink.semlink.aligner.PbVnAligner;
import io.github.semlink.semlink.aligner.PbVnAlignment;
import io.github.semlink.semlink.aligner.RelAligner;
import io.github.semlink.semlink.aligner.RoleMappingAligner;
import io.github.semlink.semlink.aligner.SelResAligner;
import io.github.semlink.semlink.aligner.SynResAligner;
import io.github.semlink.verbnet.type.SyntacticFrame;
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

    private PbVnAlignment align(@NonNull Proposition<VnClass, PropBankArg> proposition,
        @NonNull List<PropBankPhrase> chunk,
        @NonNull SyntacticFrame frame,
        @NonNull List<MappedRoleset> rolesets) {

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

    public Optional<PbVnAlignment> align(@NonNull Proposition<VnClass, PropBankArg> prop,
        @NonNull DepTree source) {

        List<PropBankPhrase> phrases = PropBankPhrase.fromProp(prop, source);

        List<PbVnAlignment> alignments = new ArrayList<>();

        String lemma = source.get(prop.relSpan().startIndex()).feature(FeatureType.Lemma);
        List<MappedRoleset> rolesets = prop.predicate().sense().related().stream()
            .map(s -> mappings.rolesets(lemma, s.verbNetId().classId()))
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

        // enumerate VerbNet frames
        for (VnClass cls : prop.predicate().sense().related()) {

            // iterate over individual frames
            for (VnFrame frame : cls.frames()) {

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

    public static PropBankVerbNetAligner of(@NonNull String mappingsPath, @NonNull String pbIndexPath) {
        try {
            PbVnMappings mappings = new PbVnMappings(PbVnMapping.fromJson(new FileInputStream(mappingsPath)),
                    DefaultPbIndex.fromBinary(pbIndexPath));
            return new PropBankVerbNetAligner(mappings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
