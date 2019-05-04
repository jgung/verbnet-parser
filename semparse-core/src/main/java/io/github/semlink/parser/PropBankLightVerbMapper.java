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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnIndex;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.util.TsvUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Light verb proposition mapper.
 *
 * @author jgung
 */
@Slf4j
public class PropBankLightVerbMapper {

    private Map<String, Map<String, VnClass>> mappings;
    private SemanticRoleLabeler<PropBankArg> semanticRoleLabeler;

    public PropBankLightVerbMapper(Map<String, Map<String, VnClass>> mappings,
                                   SemanticRoleLabeler<PropBankArg> semanticRoleLabeler) {
        this.mappings = mappings;
        this.semanticRoleLabeler = semanticRoleLabeler;
    }

    /**
     * Check if the given proposition corresponds to a light verb, and map to the corresponding nominal propositional structure
     * if so. For example, "[John] rel[took] [a look at his phone]" may map to "[John] took a rel[look] [at his phone]".
     * Maps the sense to a corresponding VerbNet class, e.g look-30.3.
     *
     * @param tree dependency tree necessary to get lemmas for light verb mappings
     * @param rel  rel node within dependency tree
     * @return optional mapped nominal proposition
     */
    public Optional<Proposition<VnClass, PropBankArg>> mapProp(@NonNull DepTree tree, @NonNull DepNode rel) {
        String verb = rel.feature(FeatureType.Lemma);
        Map<String, VnClass> lvMappings = mappings.get(verb);
        if (null == lvMappings) {
            return Optional.empty();
        }
        for (Map.Entry<String, VnClass> lemma : lvMappings.entrySet()) {
            for (DepNode child : rel.children()) {
                if (lemma.getKey().equals(child.feature(FeatureType.Lemma))) {
                    Proposition<DepNode, PropBankArg> lightProp = semanticRoleLabeler.parse(
                            tree, Collections.singletonList(child.index())).get(0);
                    if (lightProp.arguments().spans().size() > 1) {
                        return Optional.of(new Proposition<>(child.index(), lemma.getValue(), lightProp.arguments()));
                    }
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Load light verb mappings in the format: verb TAB noun TAB class, e.g. "give	bath	41.1.1".
     *
     * @return map from verb lemma, to a map from noun lemmas to VerbNet classes
     */
    public static Map<String, Map<String, VnClass>> fromMappingsPath(@NonNull String mappingsPath,
                                                                     @NonNull VnIndex verbNet) {
        try {
            Map<String, Map<String, String>> verbNounClassMap = TsvUtils.tsv2Map(mappingsPath, 0, 1, 2);
            Map<String, Map<String, VnClass>> result = new HashMap<>();
            for (Map.Entry<String, Map<String, String>> entry : verbNounClassMap.entrySet()) {
                Map<String, VnClass> clsMap = new HashMap<>();
                for (Map.Entry<String, String> nounClass : entry.getValue().entrySet()) {
                    VnClass byId = verbNet.getById(nounClass.getValue());
                    if (null != byId) {
                        clsMap.put(nounClass.getKey(), byId);
                    } else {
                        log.warn("Missing LV mapping: {}-{}", nounClass.getKey(), nounClass.getValue());
                    }
                }
                result.put(entry.getKey(), clsMap);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
