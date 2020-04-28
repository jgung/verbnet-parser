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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.app.Span;
import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.VnIndex;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static io.github.semlink.util.TsvUtils.readTsv;

/**
 * Check if the given proposition corresponds to a light verb, and map to the corresponding nominal propositional structure
 * if so. For example, "[John] rel[took] [a look at his phone]" may map to "[John] took a rel[look] [at his phone]".
 * Maps the sense to a corresponding VerbNet class, e.g look-30.3.
 *
 * @author jgung
 */
@Slf4j
@AllArgsConstructor
public class LightVerbMapper implements PredicateMapper<VnClass> {

    private Map<String, Map<String, VnMember>> mappings;

    @Override
    public Optional<Span<VnClass>> mapPredicate(@NonNull DepNode rel) {
        String verb = rel.feature(FeatureType.Lemma);
        Map<String, VnMember> lvMappings = mappings.get(verb);
        if (null == lvMappings) {
            return Optional.empty();
        }
        for (Map.Entry<String, VnMember> lemma : lvMappings.entrySet()) {
            for (DepNode child : rel.children()) {
                if (lemma.getKey().equals(child.feature(FeatureType.Lemma))) {
                    // TODO: extract out this side effect (or update PropBank mapping logic to support nominal lemmas)
                    child.addFeature(FeatureType.Lemma, lemma.getValue().lemma);
                    return Optional.of(new Span<>(lemma.getValue().vnClass, child.index(), child.index()));
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
    public static LightVerbMapper fromMappingsPath(@NonNull String mappingsPath,
                                                   @NonNull VnIndex verbNet) {
        try {
            Map<String, Map<String, VnMember>> result = new HashMap<>();
            for (String[] fields : readTsv(mappingsPath)) {
                if (fields.length < 4) {
                    continue;
                }
                String lightVerb = fields[0];
                String heavyNoun = fields[1];
                String verbClass = fields[2];
                String verbLemma = fields[3];


                VnClass byId = verbNet.getById(verbClass);
                if (null == byId) {
                    log.warn("Missing LV mapping: {}-{}", heavyNoun, verbClass);
                    continue;
                }

                Map<String, VnMember> nounClass = result.computeIfAbsent(lightVerb, ignored -> new HashMap<>());
                nounClass.put(heavyNoun, new VnMember(byId, verbLemma));
            }
            return new LightVerbMapper(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    private static class VnMember {
        private VnClass vnClass;
        private String lemma;
    }

}
