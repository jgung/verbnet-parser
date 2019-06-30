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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.semlink.verbnet.DefaultVnIndex;
import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.VnIndex;
import io.github.semlink.propbank.DefaultPbIndex;
import io.github.semlink.propbank.frames.Frameset;
import io.github.semlink.propbank.frames.PbRole;
import io.github.semlink.propbank.frames.Predicate;
import io.github.semlink.propbank.frames.Roleset;
import io.github.semlink.propbank.frames.RolesetAlias;
import io.github.semlink.propbank.frames.VerbNetRole;
import io.github.semlink.util.TsvUtils;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.extern.slf4j.Slf4j;

import static io.github.semlink.propbank.frames.FramesetFactory.deserializeFrames;

/**
 * Utility class for generating VerbNet mappings from frames.
 *
 * @author jgung
 */
@Slf4j
public final class VerbNetMappings {

    private static final ObjectMapper OM = new ObjectMapper();

    private VerbNetMappings() {
    }

    private static PbVnMapping.RolesetMapping getRolesetMappings(Roleset roleset) {
        PbVnMapping.RolesetMapping rsMapping = new PbVnMapping.RolesetMapping().id(roleset.id());

        Multimap<String, PbVnMapping.MappedRole> vnclsRoleMap = LinkedHashMultimap.create();
        for (PbRole role : roleset.roles().roles()) {
            for (VerbNetRole vnRole : role.verbNetRoles()) {
                vnclsRoleMap.put(vnRole.verbNetClass(),
                        new PbVnMapping.MappedRole()
                                .number(role.number())
                                .vntheta(vnRole.thematicRole()));
            }
        }

        for (Map.Entry<String, Collection<PbVnMapping.MappedRole>> entry : vnclsRoleMap.asMap().entrySet()) {
            PbVnMapping.RolesMapping rolesMapping = new PbVnMapping.RolesMapping()
                    .vncls(entry.getKey())
                    .roles(new HashSet<>(entry.getValue()));
            rsMapping.mappings().add(rolesMapping);
        }
        return rsMapping;
    }

    public static void writeMappings(String framesPath, String mappingsOutputPath) throws IOException {
        List<Frameset> frames = deserializeFrames(new ByteArrayInputStream(Files.readAllBytes(Paths.get(framesPath))));

        Map<String, PbVnMapping> mappings = new HashMap<>();
        for (Frameset frameset : frames) {
            for (Predicate predicate : frameset.predicates()) {
                PbVnMapping mapping = mappings.computeIfAbsent(predicate.lemma(),
                        lemma -> new PbVnMapping().lemma(lemma));

                // if a predicate lemma is non-verbal, we need to find the corresponding verbal alias for the mapping
                ListMultimap<RolesetAlias.AliasPos, String> posLemmaMap = LinkedListMultimap.create();
                for (Roleset roleset : predicate.rolesets()) {
                    PbVnMapping.RolesetMapping rsMapping = getRolesetMappings(roleset);
                    if (roleset.aliases().stream()
                            .map(RolesetAlias::pos)
                            .noneMatch(s -> s == RolesetAlias.AliasPos.V)) {
                        // if no verbs in this roleset, no need to add to mappings
                        continue;
                    }
                    roleset.aliases().forEach(alias -> posLemmaMap.put(alias.pos(), alias.lemma()));
                    if (!rsMapping.mappings().isEmpty()) {
                        mapping.mappings().add(rsMapping);
                    }
                }
                List<String> verbalLemmas = posLemmaMap.get(RolesetAlias.AliasPos.V);
                if (!posLemmaMap.get(RolesetAlias.AliasPos.V).contains(predicate.lemma())) {
                    if (verbalLemmas.size() > 0) {
                        System.out.println("Swapping mapping lemma " + predicate.lemma() + " -> " + verbalLemmas.get(0));
                        mapping.lemma(verbalLemmas.get(0));
                    }
                }
            }

        }

        List<PbVnMapping> collect = mappings.values().stream()
                .filter(mapping -> !mapping.mappings().isEmpty())
                .sorted(Comparator.comparing(PbVnMapping::lemma))
                .collect(Collectors.toList());

        log.debug("Read {} mappings", collect.size());

        OM.writerWithDefaultPrettyPrinter().writeValue(new File(mappingsOutputPath), collect);
    }

    public static void incompleteMappings(String mappingsOutputPath) throws IOException {
        Map<String, Map<String, String>> roleset2Class = TsvUtils.tsv2Map("src/main/resources/pb-vn-mappings.tsv", 0, 1, 2);
        Map<String, Map<String, String>> role2Role = TsvUtils.tsv2Map("src/main/resources/role-mappings.tsv", 0, 1, 2);

        List<PbVnMapping> result = PbVnMapping.fromJson(new FileInputStream(mappingsOutputPath));
        VnIndex verbNet = new DefaultVnIndex();

        PbVnMappings mappings = new PbVnMappings(result, DefaultPbIndex.fromBinary(
                "src/main/resources/propbank/unified-frames.bin"));

        List<String> results = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<PbVnMappings.MappedRoleset>>> lemma : mappings.lemmaClassRolesetMap().entrySet()) {

            for (Map.Entry<String, List<PbVnMappings.MappedRoleset>> vncls : lemma.getValue().entrySet()) {

                for (PbVnMappings.MappedRoleset rs : vncls.getValue()) {

                    Map<String, String> vnMappings = roleset2Class.getOrDefault(rs.id(), Collections.emptyMap());
                    String corrected = vnMappings.get(vncls.getKey());
                    String clsId = vncls.getKey();
                    if (null != corrected && verbNet.getById(corrected) != null) {
                        clsId = corrected;
                    }

                    VnClass id = verbNet.getById(clsId);
                    if (null == id) {
                        Set<VnClass> partialMatches = verbNet.getByBaseIdAndLemma(clsId, lemma.getKey());
                        if (partialMatches.size() > 0) {
                            results.add(String.format("%s\t%s\t%s\t%s\t%s",
                                    clsId, vncls.getKey(), rs.id(), "Partial match", partialMatches.stream()
                                            .map(cls -> cls.verbNetId().classId())
                                            .distinct()
                                            .sorted()
                                            .collect(Collectors.joining(", "))));
                        } else {
                            results.add(String.format("%s\t%s\t%s\t%s\t%s",
                                    clsId, vncls.getKey(), rs.id(), "Missing class",
                                    verbNet.getByLemma(lemma.getKey()).stream()
                                            .map(cls -> cls.verbNetId().classId())
                                            .distinct()
                                            .sorted()
                                            .collect(Collectors.joining(", "))));
                        }
                    }

                    final String mappedId = clsId;
                    if (id != null) {
                        Set<VnClass> verbNetClasses = verbNet.getByLemma(lemma.getKey());
                        if (!verbNetClasses.contains(id)) {
                            boolean found = false;
                            for (VnClass cls : verbNetClasses) {
                                if (cls.related().contains(id)) {
                                    found = true;
                                }
                            }
                            if (!found) {
                                results.add(String.format("%s\t%s\t%s\t%s\t%s",
                                        mappedId, vncls.getKey(), rs.id(), "Missing lemma", ""));
                            }
                        } else {

                            Map<String, String> roleFixes = role2Role
                                    .getOrDefault(id.verbNetId().classId(), Collections.emptyMap());

                            Set<String> mappedRoles = rs.roleMappings().values().stream()
                                    .flatMap(Collection::stream)
                                    .map(String::toLowerCase)
                                    .map(r -> roleFixes.getOrDefault(r, r))
                                    .collect(Collectors.toSet());

                            Set<String> roles =
                                    id.ancestors().stream().map(r -> r.roles().stream()
                                            .map(role -> role.type().toLowerCase())
                                            .collect(Collectors.toSet()))
                                            .flatMap(Set::stream)
                                            .collect(Collectors.toSet());

                            String missing = String.join(", ", Sets.difference(mappedRoles, roles));
                            if (missing.length() > 0) {
                                results.add(String.format("%s\t%s\t%s\t%s\t%s\t%s",
                                        mappedId, vncls.getKey(), rs.id(), "Missing role mappings", String.join(", ", roles),
                                        missing));
                            }

                        }
                    }
                }
            }
        }
        System.out.println("Mapped ID\tID\tRoleset\tType\tPossible Mappings\tCorrections\tMissing Roles");
        results.stream().sorted().forEach(System.out::println);
    }

    public static void outputUpdatedMappings(String mappingsOutputPath) throws IOException {
        Map<String, Map<String, String>> roleset2Class = TsvUtils.tsv2Map("data/pb-vn-mappings.tsv", 0, 1, 2);
        Map<String, Map<String, String>> role2Role = TsvUtils.tsv2Map("data/role-mappings.tsv", 0, 1, 2);

        List<PbVnMapping> result = PbVnMapping.fromJson(new FileInputStream(mappingsOutputPath));
        VnIndex verbNet = new DefaultVnIndex();

        List<PbVnMapping> updatedMappings = new ArrayList<>();

        for (PbVnMapping mapping : result) {
            String lemma = mapping.lemma();
            List<PbVnMapping.RolesetMapping> newRolesetMappings = new ArrayList<>();
            for (PbVnMapping.RolesetMapping rolesetMapping : mapping.mappings()) {
                String rolesetId = rolesetMapping.id();
                Map<String, PbVnMapping.RolesMapping> rolesMappings = new LinkedHashMap<>();
                for (PbVnMapping.RolesMapping rolesMapping : rolesetMapping.mappings()) {
                    String clsId = rolesMapping.vncls();

                    Map<String, String> vnMappings = roleset2Class.getOrDefault(rolesetId, Collections.emptyMap());
                    String correctedVncls = vnMappings.get(clsId);
                    if (null != correctedVncls && verbNet.getById(correctedVncls) != null) {
                        clsId = correctedVncls;
                    }

                    VnClass id = verbNet.getById(clsId);

                    if (id != null) {
                        PbVnMapping.RolesMapping newRolesMapping = rolesMappings
                                .computeIfAbsent(id.verbNetId().classId(), classId -> new PbVnMapping.RolesMapping().vncls(classId));

                        List<VnClass> verbNetClasses = verbNet.getByLemma(lemma).stream()
                                .map(VnClass::ancestors)
                                .flatMap(List::stream)
                                .collect(Collectors.toList());

                        if (verbNetClasses.contains(id)) {
                            Map<String, String> roleFixes = role2Role
                                    .getOrDefault(id.verbNetId().classId(), Collections.emptyMap());

                            Set<String> roles =
                                    id.ancestors().stream().map(r -> r.roles().stream()
                                            .map(role -> ThematicRoleType.fromString(role.type()).orElse(ThematicRoleType.NONE).toString())
                                            .collect(Collectors.toSet()))
                                            .flatMap(Set::stream)
                                            .collect(Collectors.toSet());

                            for (PbVnMapping.MappedRole mapped : rolesMapping.roles()) {
                                String role = roleFixes.getOrDefault(
                                        ThematicRoleType.fromString(mapped.vntheta().toLowerCase()).orElse(ThematicRoleType.NONE)
                                                .toString(),
                                        ThematicRoleType.fromString(mapped.vntheta().toLowerCase()).orElse(ThematicRoleType.NONE)
                                                .toString());
                                if (!roles.contains(role)) {
                                    continue;
                                }
                                PbVnMapping.MappedRole updated = new PbVnMapping.MappedRole().number(mapped.number()).vntheta(role);
                                newRolesMapping.roles().add(updated);
                            }
                        }
                    }
                }
                if (rolesMappings.size() > 0) {
                    PbVnMapping.RolesetMapping newRoleSetMapping = new PbVnMapping.RolesetMapping().id(rolesetId)
                            .mappings(rolesMappings.values().stream().filter(s -> s.roles().size() > 0).collect(
                                    Collectors.toList()));
                    if (newRoleSetMapping.mappings().size() > 0) {
                        newRolesetMappings.add(newRoleSetMapping);
                    }
                }
            }
            if (newRolesetMappings.size() > 0) {
                updatedMappings.add(new PbVnMapping().lemma(lemma).mappings(newRolesetMappings));
            }
        }
        List<PbVnMapping> collect = updatedMappings.stream()
                .filter(mapping -> !mapping.mappings().isEmpty())
                .sorted(Comparator.comparing(PbVnMapping::lemma))
                .collect(Collectors.toList());

        log.debug("Read {} mappings", collect.size());

        OM.writerWithDefaultPrettyPrinter().writeValue(new File(mappingsOutputPath + ".updated.json"), collect);
    }

    public static void main(String[] args) throws IOException {
        String framesPath = "data/propbank-frames.bin";
        String outPath = "data/pbvn-mappings.json";
        writeMappings(framesPath, outPath);
        incompleteMappings(outPath);
        outputUpdatedMappings(outPath);
    }

}
