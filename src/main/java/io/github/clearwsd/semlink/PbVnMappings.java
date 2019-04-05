package io.github.clearwsd.semlink;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.clearwsd.propbank.type.ArgNumber;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * PropBank VerbNet mappings. Lemma -> VerbNet classes -> PropBank rolesets -> VerbNet roles.
 *
 * @author jgung
 */
@Accessors(fluent = true)
public class PbVnMappings {

    @Getter
    private Map<String, Map<String, List<Roleset>>> lemmaClassRolesetMap = new HashMap<>();

    public PbVnMappings(Collection<PbVnMapping> mappings) {

        // map from lemma to mappings
        Multimap<String, PbVnMapping> lemmaMapping = Multimaps.index(mappings, PbVnMapping::lemma);
        for (String lemma : lemmaMapping.keySet()) {

            // map from rolesets to roleset mappings
            Map<String, List<Roleset>> classRolesetMap = lemmaClassRolesetMap.computeIfAbsent(lemma, ignored -> new HashMap<>());

            lemmaMapping.get(lemma).stream()
                    .map(PbVnMapping::mappings)
                    .flatMap(List::stream)
                    .forEach(mapping -> {

                        for (PbVnMapping.RolesMapping rolesMapping : mapping.mappings()) {

                            Roleset roleset = new Roleset()
                                    .id(mapping.id())
                                    .roleMappings(rolesMapping.roles().stream()
                                            .collect(ImmutableListMultimap.toImmutableListMultimap(PbVnMapping.MappedRole::number,
                                                    PbVnMapping.MappedRole::vntheta)).asMap());

                            classRolesetMap.computeIfAbsent(rolesMapping.vncls(), cls -> new ArrayList<>()).add(roleset);
                        }

                    });

        }
    }

    public List<Roleset> rolesets(@NonNull String lemma, @NonNull String verbClass) {
        return lemmaClassRolesetMap.getOrDefault(lemma, Collections.emptyMap())
                .getOrDefault(verbClass, Collections.emptyList());
    }

    @Data
    @Accessors(fluent = true)
    public static class Roleset {
        private String id;
        private Map<ArgNumber, Collection<String>> roleMappings;
    }

}
