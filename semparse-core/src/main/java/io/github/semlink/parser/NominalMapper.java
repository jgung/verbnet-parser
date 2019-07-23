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
import java.util.stream.Collectors;

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
 * Check if an argument of the input corresponds to a nominal predicate, and map to the corresponding nominal propositional
 * structure if so.
 *
 * @author jgung
 */
@Slf4j
@AllArgsConstructor
public class NominalMapper implements PredicateMapper<VnClass> {

    private Map<String, VnMember> mappings;

    @Override
    public Optional<Span<VnClass>> mapPredicate(@NonNull DepNode child) {
        if (!child.feature(FeatureType.Pos).toString().toUpperCase().startsWith("N")
                || child.feature(FeatureType.Dep).toString().toUpperCase().equals("COMPOUND")) {
            return Optional.empty();
        }

        String lemma = child.feature(FeatureType.Lemma);
        VnMember member = mappings.get(lemma);
        if (null != member) {
            child.addFeature(FeatureType.Lemma, member.lemma);
            return Optional.of(new Span<>(member.vnClass, child.index(), child.index()));
        }
        return Optional.empty();
    }

    /**
     * Load mappings in the format: noun TAB verb TAB class, e.g. "adjustment   adjust  26.9".
     *
     * @return map from verb lemma, to a map from noun lemmas to VerbNet classes
     */
    public static NominalMapper fromMappingsPath(@NonNull String mappingsPath,
                                                 @NonNull VnIndex verbNet) {
        try {
            Map<String, VnMember> result = new HashMap<>();
            for (String[] fields : readTsv(mappingsPath)) {
                if (fields.length < 3) {
                    continue;
                }
                String noun = fields[0];
                String verbLemma = fields[1];
                String verbClasses = fields[2];
                for (String verbClass : verbClasses.split(" ")) {
                    VnClass byId = verbNet.getById(verbClass);
                    if (null == byId) {
                        log.warn("Missing VerbNet class for nominal mapping: {}-{}", verbLemma, verbClass);
                        continue;
                    }
                    if (verbNet.getMembersByLemma(verbLemma).stream()
                            .map(io.github.semlink.verbnet.VnMember::verbClass)
                            .collect(Collectors.toSet()).contains(byId)) {
                        result.put(noun, new VnMember(byId, verbLemma));
                        break;
                    }
                }
            }
            return new NominalMapper(result);
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
