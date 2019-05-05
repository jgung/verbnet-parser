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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.verbnet.DefaultVnIndex;
import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnIndex;
import io.github.semlink.app.Span;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.VerbNetAligner;
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
public class VerbNetParser implements SemanticRoleLabeler<PropBankArg> {

    @Delegate
    private SemanticRoleLabeler<PropBankArg> roleLabeler;
    private VerbNetSenseClassifier classifier;
    private VerbNetAligner aligner;

    private LightVerbMapper lightVerbMapper;

    public VerbNetParser(@NonNull VerbNetSenseClassifier classifier,
                         @NonNull SemanticRoleLabeler<PropBankArg> roleLabeler,
                         @NonNull VerbNetAligner aligner,
                         @NonNull LightVerbMapper lightVerbMapper) {
        this.roleLabeler = roleLabeler;
        this.classifier = classifier;
        this.aligner = aligner;
        this.lightVerbMapper = lightVerbMapper;
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

        return aligner.align(parsed, props);
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
        LightVerbMapper verbMapper = LightVerbMapper.fromMappingsPath(lightVerbMappings, verbNet);
        VerbNetAligner aligner = VerbNetAligner.of(mappingsPath, propbank);

        VerbNetParser parser = new VerbNetParser(classifier, roleLabeler, aligner, verbMapper);

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
