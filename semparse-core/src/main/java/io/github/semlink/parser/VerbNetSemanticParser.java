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

package io.github.semlink.parser;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.verbnet.DefaultVnIndex;
import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnIndex;
import io.github.semlink.app.Span;
import io.github.semlink.propbank.frames.PbRole;
import io.github.semlink.propbank.frames.Roleset;
import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.semlink.PropBankVerbNetAligner;
import io.github.semlink.semlink.SemlinkRole;
import io.github.semlink.verbnet.type.NounPhrase;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import static io.github.semlink.parser.SemanticRoleLabeler.convert;

/**
 * VerbNet semantic parser.
 *
 * @author jgung
 */
@Slf4j
public class VerbNetSemanticParser implements SemanticRoleLabeler<PropBankArg> {

    private VerbNetSenseClassifier classifier;
    @Delegate
    private SemanticRoleLabeler<PropBankArg> roleLabeler;

    private PropBankVerbNetAligner aligner;
    private LightVerbMapper lightVerbMapper;
    private VnPredicateExtractor predicateExtractor = new VnPredicateExtractor();

    public VerbNetSemanticParser(@NonNull VerbNetSenseClassifier classifier,
                                 @NonNull SemanticRoleLabeler<PropBankArg> roleLabeler,
                                 @NonNull PropBankVerbNetAligner aligner,
                                 @NonNull LightVerbMapper lightVerbMapper) {
        this.roleLabeler = roleLabeler;
        this.classifier = classifier;
        this.aligner = aligner;
        this.lightVerbMapper = lightVerbMapper;
    }

    public VerbNetSemanticParse align(@NonNull DepTree parsed,
                                      @NonNull List<Proposition<VnClass, PropBankArg>> props) {
        List<String> tokens = parsed.stream().map(node -> (String) node.feature(FeatureType.Text)).collect(Collectors.toList());

        VerbNetSemanticParse parse = new VerbNetSemanticParse()
                .tokens(tokens)
                .tree(parsed);
        for (Proposition<VnClass, PropBankArg> prop : props) {
            if (prop.predicate() == null) {
                continue;
            }
            parse.props().add(alignProp(prop, parsed, tokens));
        }

        return parse;
    }

    private DefaultVerbNetProp alignProp(Proposition<VnClass, PropBankArg> prop, DepTree parsed, List<String> tokens) {
        DefaultVerbNetProp vnProp = new DefaultVerbNetProp()
                .proposition(SemlinkRole.convert(prop))
                .tokens(tokens);

        aligner.align(prop, parsed).ifPresent(aligned -> {
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

    public VerbNetSemanticParse parseSentence(@NonNull DepTree parsed,
                                              @NonNull List<SensePrediction<VnClass>> senses) {
        List<VnClass> vnClasses = new ArrayList<>();
        List<Integer> predicateIndices = new ArrayList<>();
        for (SensePrediction<VnClass> sense : senses) {
            DepNode verb = parsed.get(sense.index());

            // map light verbs to nominal props
            Optional<Span<VnClass>> possibleHeavyNoun = lightVerbMapper.mapVerb(verb);
            if (possibleHeavyNoun.isPresent()) {
                vnClasses.add(possibleHeavyNoun.get().label());
                predicateIndices.add(possibleHeavyNoun.get().startIndex());
            } else {
                // otherwise just use verbal prop
                vnClasses.add(sense.sense());
                predicateIndices.add(sense.index());
            }
        }

        List<Proposition<VnClass, PropBankArg>> props = convert(roleLabeler.parse(parsed, predicateIndices), vnClasses);

        return align(parsed, props);
    }

    public VerbNetSemanticParse parseSentence(@NonNull DepTree parsed) {
        List<SensePrediction<VnClass>> senses = classifier.predict(parsed);
        return parseSentence(parsed, senses);
    }

    public VerbNetSemanticParse parseSentence(@NonNull String sentence) {
        final DepTree parsed = classifier.parse(classifier.tokenize(sentence));
        return parseSentence(parsed);
    }

    public List<VerbNetSemanticParse> parseDocument(@NonNull String document) {
        return classifier.segment(document).stream().map(this::parseSentence).collect(Collectors.toList());
    }

    public static SemanticRoleLabeler<PropBankArg> roleLabeler(@NonNull String modelPath) {
        return new DefaultSemanticRoleLabeler<>(RoleLabelerUtils.shallowSemanticParser(modelPath), PropBankArg::fromLabel);
    }

    public static void main(String[] args) {
        String mappingsPath = "data/pbvn-mappings.json.updated.json";
        String modelDir = "data/models/unified-propbank";
        String wsdModel = "data/models/verbnet/nlp4j-verbnet-3.3.bin";
        String lightVerbMappings = "semparse-core/src/main/resources/lvm.tsv";
        String propbank = "data/unified-frames.bin";

        SemanticRoleLabeler<PropBankArg> roleLabeler = roleLabeler(modelDir);
        VnIndex verbNet = new DefaultVnIndex();
        VerbNetSenseClassifier classifier = VerbNetSenseClassifier.fromModelPath(wsdModel, verbNet);
        PropBankVerbNetAligner aligner = PropBankVerbNetAligner.of(mappingsPath, propbank);
        LightVerbMapper verbMapper = LightVerbMapper.fromMappingsPath(lightVerbMappings, verbNet);

        VerbNetSemanticParser parser = new VerbNetSemanticParser(classifier, roleLabeler, aligner, verbMapper);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print(">> ");
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("quit")) {
                    break;
                }
                parser.parseDocument(line)
                        .forEach(System.out::println);
            } catch (Exception e) {
                log.warn("An unexpected error occurred", e);
            }
        }
    }

}
