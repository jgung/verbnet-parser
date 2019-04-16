package io.github.semlink.semlink;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.semlink.propbank.PbIndex;
import io.github.semlink.propbank.frames.Roleset;
import io.github.semlink.propbank.type.ArgNumber;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

/**
 * PropBank VerbNet mappings. Lemma -> VerbNet classes -> PropBank rolesets -> VerbNet roles.
 *
 * @author jgung
 */
@Accessors(fluent = true)
public class PbVnMappings {

    @Getter
    private Map<String, Map<String, List<MappedRoleset>>> lemmaClassRolesetMap = new HashMap<>();
    @Getter
    private PbIndex pbIndex;

    public PbVnMappings(Collection<PbVnMapping> mappings, PbIndex pbIndex) {
        this.pbIndex = pbIndex;
        // map from lemma to mappings
        Multimap<String, PbVnMapping> lemmaMapping = Multimaps.index(mappings, PbVnMapping::lemma);
        for (String lemma : lemmaMapping.keySet()) {

            // map from rolesets to roleset mappings
            Map<String, List<MappedRoleset>> classRolesetMap = lemmaClassRolesetMap.computeIfAbsent(lemma, ignored -> new HashMap<>());

            lemmaMapping.get(lemma).stream()
                    .map(PbVnMapping::mappings)
                    .flatMap(List::stream)
                    .forEach(mapping -> {

                        for (PbVnMapping.RolesMapping rolesMapping : mapping.mappings()) {

                            MappedRoleset roleset = new MappedRoleset()
                                    .roleset(pbIndex.getById(mapping.id()))
                                    .roleMappings(rolesMapping.roles().stream()
                                            .collect(ImmutableListMultimap.toImmutableListMultimap(PbVnMapping.MappedRole::number,
                                                    PbVnMapping.MappedRole::vntheta)).asMap());

                            classRolesetMap.computeIfAbsent(rolesMapping.vncls(), cls -> new ArrayList<>()).add(roleset);
                        }

                    });

        }
    }

    public List<MappedRoleset> rolesets(@NonNull String lemma, @NonNull String verbClass) {
        return lemmaClassRolesetMap.getOrDefault(lemma, Collections.emptyMap())
                .getOrDefault(verbClass, Collections.emptyList());
    }

    @Data
    @Accessors(fluent = true)
    public static class MappedRoleset {
        @Delegate
        private Roleset roleset;
        private Map<ArgNumber, Collection<String>> roleMappings;

    }

}
