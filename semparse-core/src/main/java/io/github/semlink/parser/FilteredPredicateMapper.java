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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.app.Span;
import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.VnIndex;
import io.github.semlink.verbnet.VnMember;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static io.github.semlink.util.TsvUtils.readTsv;

/**
 * Check if an argument of the input corresponds to a predicate, and map to the corresponding propositional
 * structure if so.
 *
 * @author jgung
 */
@Slf4j
@AllArgsConstructor
public class FilteredPredicateMapper implements PredicateMapper<VnClass> {

    private Map<String, MappedMember> mappings;
    private Predicate<DepNode> filter;

    @Override
    public Optional<Span<VnClass>> mapPredicate(@NonNull DepNode child) {
        if (!filter.test(child)) {
            return Optional.empty();
        }

        String lemma = child.feature(FeatureType.Lemma);
        MappedMember member = mappings.get(lemma);
        if (null != member) {
            child.addFeature(FeatureType.Lemma, member.lemma);
            return Optional.of(new Span<>(member.vnClass, child.index(), child.index()));
        }
        return Optional.empty();
    }

    /**
     * Load mappings in the format: noun/adjective TAB verb TAB class, e.g. "adjustment   adjust  26.9".
     *
     * @return map from verb lemma, to a map from noun/adjectival lemmas to VerbNet classes
     */
    public static FilteredPredicateMapper fromMappingsPath(@NonNull String mappingsPath,
                                                           @NonNull VnIndex verbNet,
                                                           @NonNull Predicate<DepNode> filter) {
        try {
            Map<String, MappedMember> result = new HashMap<>();
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
                        log.debug("Missing VerbNet class for predicate mapping: {}-{}", verbLemma, verbClass);
                        continue;
                    }
                    Set<VnMember> members = verbNet.getMembersByLemma(verbLemma);
                    if (members.stream()
                            .map(VnMember::verbClass)
                            .map(VnClass::related)
                            .flatMap(List::stream)
                            .collect(Collectors.toSet()).contains(byId)) {
                        Optional<VnClass> first = byId.related().stream()
                                .filter(s -> s.members().stream().anyMatch(m -> m.name().equalsIgnoreCase(verbLemma)))
                                .findFirst();
                        result.put(noun, new MappedMember(first.orElse(byId), verbLemma));
                        break;
                    } else if (members.size() == 1) {
                        VnClass vnClass = members.iterator().next().verbClass();
                        log.debug("Lemma not found in VerbNet class: {}-{}, mapping to {}", verbLemma, verbClass,
                                vnClass.verbNetId());
                        result.put(noun, new MappedMember(vnClass, verbLemma));
                    } else if (members.size() == 0) {
                        log.debug("Lemma not found in VerbNet class: {}-{}, mapping to {}", verbLemma, verbClass,
                                byId.verbNetId());
                        result.put(noun, new MappedMember(byId, verbLemma));
                    } else {
                        log.debug("Lemma not found in VerbNet class: {}-{}", verbLemma, verbClass);
                    }
                }
            }
            return new FilteredPredicateMapper(result, filter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    private static class MappedMember {
        private VnClass vnClass;
        private String lemma;
    }

}
