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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.verbnet.VnClass;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.VerbNetAligner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Facade around VerbNet parsing components (VerbNet class disambiguation, semantic role labeling, frame alignment, etc.).
 *
 * @author jgung
 */
@Slf4j
@Getter
@AllArgsConstructor
public class VerbNetParser {

    private VerbNetSenseClassifier verbNetClassifier;

    private VerbNetSemParser verbNetRoleLabeler;

    public VerbNetParser(@NonNull VerbNetSenseClassifier verbNetClassifier,
                         @NonNull SemanticRoleLabeler<PropBankArg> roleLabeler,
                         @NonNull VerbNetAligner aligner,
                         @NonNull PredicateMapper<VnClass> predicateMapper) {
        this.verbNetClassifier = verbNetClassifier;
        this.verbNetRoleLabeler = new VerbNetSemParser(roleLabeler, aligner, Collections.singletonList(predicateMapper));
    }

    /**
     * Generate a {@link VerbNetParse} from a {@link DepTree dependency parse} for a list of specific verbs/predicates linked to
     * VerbNet classes. Performs semantic role labeling and alignment to VerbNet frames.
     *
     * @param parsed dependency parse
     * @param senses predicates (verbs)
     * @return VerbNet semantic parse
     */
    public VerbNetParse parse(@NonNull DepTree parsed,
                              @NonNull List<SensePrediction<VnClass>> senses) {
        List<VerbNetProp> vnProps = verbNetRoleLabeler.extractProps(parsed, senses);

        return new VerbNetParse()
                .tokens(parsed.stream()
                        .map(node -> (String) node.feature(FeatureType.Text))
                        .collect(Collectors.toList()))
                .tree(parsed)
                .props(vnProps.stream().map(prop -> (DefaultVerbNetProp) prop).collect(Collectors.toList()));
    }

    /**
     * Generate a {@link VerbNetParse} from a {@link DepTree dependency parse}. Performs VerbNet classification to identify
     * predicates and their corresponding VerbNet classes. Then performs semantic role labeling and alignment to VerbNet frames.
     *
     * @param parsed dependency parse
     * @return VerbNet semantic parse
     */
    public VerbNetParse parse(@NonNull DepTree parsed) {
        List<SensePrediction<VnClass>> senses = verbNetClassifier.predict(parsed);
        return parse(parsed, senses);
    }

    /**
     * Generate a {@link VerbNetParse} from a raw, untokenized input sentence. Performs VerbNet classification to identify
     * predicates and their corresponding VerbNet classes. Then performs semantic role labeling and alignment to VerbNet frames.
     *
     * @param sentence raw input sentence
     * @return VerbNet semantic parse
     */
    public VerbNetParse parse(@NonNull String sentence) {
        List<String> tokens = verbNetClassifier.tokenize(sentence);
        DepTree depTree = verbNetClassifier.parse(tokens);
        return parse(depTree);
    }

    /**
     * Instantiate a new {@link SemanticRoleLabeler} for PropBank from a given model path.
     */
    public static SemanticRoleLabeler<PropBankArg> pbRoleLabeler(@NonNull String modelPath) {
        return new DefaultSemanticRoleLabeler<>(RoleLabelerUtils.shallowSemanticParser(modelPath), PropBankArg::fromLabel);
    }

}
