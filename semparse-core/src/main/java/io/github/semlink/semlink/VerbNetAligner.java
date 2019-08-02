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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.app.Span;
import io.github.semlink.parser.DefaultVerbNetProp;
import io.github.semlink.parser.Proposition;
import io.github.semlink.parser.VerbNetProp;
import io.github.semlink.propbank.DefaultPbIndex;
import io.github.semlink.propbank.frames.PbRole;
import io.github.semlink.propbank.frames.Roleset;
import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.PbVnMappings.MappedRoleset;
import io.github.semlink.semlink.aligner.AdjustInvalidRoles;
import io.github.semlink.semlink.aligner.FillerAligner;
import io.github.semlink.semlink.aligner.PbVnAligner;
import io.github.semlink.semlink.aligner.PbVnAlignment;
import io.github.semlink.semlink.aligner.RelAligner;
import io.github.semlink.semlink.aligner.RoleMappingAligner;
import io.github.semlink.semlink.aligner.SelResAligner;
import io.github.semlink.semlink.aligner.SynResAligner;
import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.VnFrame;
import io.github.semlink.verbnet.type.NounPhrase;
import io.github.semlink.verbnet.type.SyntacticFrame;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * PropBank VerbNet role mapping service.
 *
 * @author jgung
 */
@AllArgsConstructor
public class VerbNetAligner {

    private PbVnMappings mappings;
    private List<PbVnAligner> aligners;
    private VnPredicateExtractor predicateExtractor;

    public VerbNetAligner(@NonNull PbVnMappings mappings) {
        this(mappings,
                ImmutableList.of(
                        new RelAligner(),
                        new RoleMappingAligner(),
                        new SynResAligner(),
                        new SelResAligner(),
                        new FillerAligner(),
                        new SelResAligner(SelResAligner::getThematicRolesGreedy),
                        new SynResAligner(),
                        new AdjustInvalidRoles()),
                new VnPredicateExtractor()
        );
    }

    public List<VerbNetProp> align(@NonNull DepTree parsed,
                                   @NonNull List<Proposition<VnClass, PropBankArg>> props) {
        return props.stream()
                .filter(prop -> null != prop.predicate())
                .map(prop -> alignProp(prop, parsed))
                .collect(Collectors.toList());
    }

    private VerbNetProp alignProp(Proposition<VnClass, PropBankArg> prop, DepTree parsed) {
        List<String> tokens = parsed.stream().map(node -> (String) node.feature(FeatureType.Text)).collect(Collectors.toList());

        DefaultVerbNetProp vnProp = new DefaultVerbNetProp()
                .proposition(SemlinkRole.convert(prop))
                .tokens(tokens);

        align(prop, parsed).ifPresent(aligned -> {
            // get thematic role alignment
            Preconditions.checkState(aligned.sourcePhrases().size() == prop.arguments().spans().size());

            Iterator<PropBankPhrase> propBankPhrases = aligned.sourcePhrases().iterator();
            for (Span<SemlinkRole> span : vnProp.proposition().arguments().spans()) {
                PropBankPhrase phrase = propBankPhrases.next();

                // get direct roleset mappings
                if (null != aligned.roleset()) {
                    Roleset mapped = aligned.roleset().roleset();
                    if (null != mapped) {
                        Optional<PbRole> role = mapped.getRole(span.label().propBankArg().getNumber());
                        role.ifPresent(pbRole -> span.label().pbRole(pbRole));
                    }
                }

                if (phrase.getNumber() == ArgNumber.V) {
                    span.label().thematicRoleType(ThematicRoleType.VERB);
                } else {
                    Optional<NounPhrase> nounPhrase = aligned.alignedPhrases(phrase).stream()
                            .filter(np -> np instanceof NounPhrase)
                            .map(np -> (NounPhrase) np)
                            .findFirst();
                    nounPhrase.ifPresent(np -> span.label().thematicRoleType(np.thematicRoleType()));
                }

            }
            String lemma = parsed.get(prop.relIndex()).feature(FeatureType.Lemma);
            // get semantic predicates
            vnProp.predicates(predicateExtractor.parsePredicates(aligned.alignment(), aligned.frame(),
                    prop.predicate(), lemma));
        });
        return vnProp;
    }

    private PbVnAlignment align(Proposition<VnClass, PropBankArg> proposition,
                                List<PropBankPhrase> chunk,
                                SyntacticFrame frame,
                                List<MappedRoleset> rolesets) {

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

    private Optional<PbVnAlignment> align(Proposition<VnClass, PropBankArg> prop, DepTree source) {

        List<PropBankPhrase> phrases = PropBankPhrase.fromProp(prop, source);

        List<PbVnAlignment> alignments = new ArrayList<>();

        String lemma = source.get(prop.relSpan().startIndex()).feature(FeatureType.Lemma);
        List<MappedRoleset> rolesets = prop.predicate().related().stream()
                .map(s -> mappings.rolesets(lemma, s.verbNetId().classId()))
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        // enumerate VerbNet frames
        for (VnClass cls : prop.predicate().ancestors(true)) {

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

    public static VerbNetAligner of(@NonNull String mappingsPath, @NonNull String pbIndexPath) {
        try {
            PbVnMappings mappings = new PbVnMappings(PbVnMapping.fromJson(new FileInputStream(mappingsPath)),
                    DefaultPbIndex.fromBinary(pbIndexPath));
            return new VerbNetAligner(mappings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
