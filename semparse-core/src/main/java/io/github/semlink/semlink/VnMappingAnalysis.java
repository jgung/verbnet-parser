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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.semlink.propbank.frames.Frameset;
import io.github.semlink.propbank.frames.PbRole;
import io.github.semlink.propbank.frames.Roleset;
import io.github.semlink.propbank.frames.VerbNetRole;
import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.propbank.type.FunctionTag;
import io.github.semlink.util.TsvUtils;
import io.github.semlink.verbnet.DefaultVnIndex;
import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.VnClassId;
import io.github.semlink.verbnet.VnIndex;
import io.github.semlink.verbnet.VnThematicRole;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static io.github.semlink.propbank.frames.FramesetFactory.deserializeFrames;

/**
 * Verbnet mapping analysis script.
 *
 * @author jgung
 */
@Slf4j
public final class VnMappingAnalysis {

    private VnMappingAnalysis() {
    }

    @Getter
    @Accessors(fluent = true)
    @NoArgsConstructor
    private static class VnMapping {
        private Map<VnClassId, Set<VnClass>> mapping = new HashMap<>();
        private Set<VnClass> unmapped = new HashSet<>();

        public Set<VnClass> mapped(VnClassId original) {
            return mapping.computeIfAbsent(original, o -> new HashSet<>());
        }

        public Set<VnClass> mapped(String original) {
            return mapping.computeIfAbsent(VnClassId.parse(original), o -> new HashSet<>());
        }
    }

    @Getter
    @Accessors(fluent = true)
    @AllArgsConstructor
    private static class TsvMapping {
        private String reason;
        private String roleset;
        private String originalVnClass;
        private String newVnClass;
    }

    private static List<TsvMapping> readMappings(String path) throws IOException {
        List<TsvMapping> result = new ArrayList<>();
        for (String[] entry : TsvUtils.readTsv(path)) {
            if (entry.length < 3 || entry.length > 4) {
                System.out.println(String.join("\t", entry));
            } else {
                String newVnClass = entry.length < 4 || entry[3].trim().isEmpty() ? entry[2] : entry[3];
                result.add(new TsvMapping(entry[0], entry[1], entry[2], newVnClass));
            }
        }
        return result;
    }

    public static class TsvMappings {
        private List<TsvMapping> mappings;
        private Multimap<String, TsvMapping> byRoleset;
        private Multimap<String, TsvMapping> byOriginalClass;
        private Multimap<String, TsvMapping> byNewClass;

        TsvMappings(List<TsvMapping> mappings) {
            this.mappings = mappings;
            byRoleset = Multimaps.index(mappings, TsvMapping::roleset);
            byOriginalClass = Multimaps.index(mappings, TsvMapping::originalVnClass);
            byNewClass = Multimaps.index(mappings, TsvMapping::newVnClass);
        }


        public List<TsvMapping> byReason(String reason) {
            return mappings.stream().filter(m -> m.reason.equals(reason)).collect(Collectors.toList());
        }

        public List<TsvMapping> byRoleset(String roleset) {
            return ImmutableList.copyOf(byRoleset.get(roleset));
        }

        public List<TsvMapping> byOriginalClass(String vnClass) {
            return ImmutableList.copyOf(byOriginalClass.get(vnClass));
        }

        public List<TsvMapping> byNewClass(String vnClass) {
            return ImmutableList.copyOf(byNewClass.get(vnClass));
        }

        static TsvMappings fromPath(@NonNull String path) {
            try {
                return new TsvMappings(readMappings(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Frames {
        private Map<String, Roleset> rolesetMap;

        public Frames(List<Frameset> frames) {
            rolesetMap = frames.stream()
                    .flatMap(frame -> frame.rolesets().stream())
                    .collect(Collectors.toMap(Roleset::id, Function.identity()));
        }

        public Roleset byId(@NonNull String id) {
            return rolesetMap.get(id);
        }
    }

    private static Map<FunctionTag, List<ThematicRoleType>> rolesByFt(List<Frameset> framesets) {
        Map<FunctionTag, Map<ThematicRoleType, Integer>> counts = new HashMap<>();
        for (Frameset frameset : framesets) {
            for (Roleset rs : frameset.rolesets()) {
                for (PbRole role : rs.roles().roles()) {
                    for (VerbNetRole verbNetRole : role.verbNetRoles()) {
                        Map<ThematicRoleType, Integer> roleCountMap = counts.computeIfAbsent(role.functionTag(), k -> new HashMap<>());
                        Optional<ThematicRoleType> thematicRoleType = ThematicRoleType.fromString(verbNetRole.thematicRole());
                        thematicRoleType.ifPresent(roleType -> roleCountMap.put(roleType,
                                roleCountMap.getOrDefault(roleType, 0) + 1));
                    }
                }
            }
        }
        return counts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().entrySet().stream()
                        .sorted(Comparator.comparing(e -> -e.getValue()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList())));
    }

    private static Map<String, List<String>> printFtsByRole(List<Frameset> framesets) {
        Map<String, Map<String, Integer>> counts = new HashMap<>();
        for (Frameset frameset : framesets) {
            for (Roleset rs : frameset.rolesets()) {
                for (PbRole role : rs.roles().roles()) {
                    for (VerbNetRole verbNetRole : role.verbNetRoles()) {
                        Map<String, Integer> ftCountMap = counts.computeIfAbsent(verbNetRole.thematicRole(), k -> new HashMap<>());
                        ftCountMap.put(role.functionTag().toString(),
                                ftCountMap.getOrDefault(role.functionTag().toString(), 0) + 1);
                    }
                }
            }
        }
        Comparator<Map.Entry<String, Map<String, Integer>>> comparator =
                Comparator.comparing(m -> m.getValue().values().stream().mapToInt(i -> i).sum());
        counts.entrySet().stream().sorted(comparator.reversed()).forEach(entry -> {
            int sum = entry.getValue().values().stream().mapToInt(i -> i).sum();
            System.out.println(entry.getKey() + "\t" + sum);
            Comparator<Map.Entry<String, Integer>> comp = Comparator.comparing(Map.Entry::getValue);
            entry.getValue().entrySet().stream().sorted(comp.reversed()).forEach(
                    s -> System.out.println("\t" + s.getKey() + "\t" + ((double) s.getValue() / sum) + "\t" + s.getValue())
            );
        });

        return counts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().entrySet().stream()
                        .sorted(Comparator.comparing(e -> -e.getValue()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList())));
    }

    @Data
    @AllArgsConstructor
    public static class RolesetVn {
        String roleset;
        String vn;
    }

    private static Map<String, VnMapping> getMappings(List<Frameset> framesets,
                                                      VnIndex vnIndex,
                                                      String tsvMappingsPath,
                                                      boolean printMissing) {
        TsvMappings tsvMappings = TsvMappings.fromPath(tsvMappingsPath);
        Frames frames = new Frames(framesets);

        Set<RolesetVn> rsVNPairs = new HashSet<>();

        Map<String, VnMapping> rolesetMappings = new HashMap<>();
        List<String> mappingLines = new ArrayList<>();
        for (TsvMapping mapping : tsvMappings.mappings) {
            String roleset = mapping.roleset;

            VnMapping vnMapping = rolesetMappings.computeIfAbsent(roleset, rs -> new VnMapping());
            Set<VnClass> mappings;

            if (null == mapping.originalVnClass) {
                mappings = vnMapping.unmapped;
            } else {
                try {
                    VnClassId id = VnClassId.parse(mapping.originalVnClass());
                    mappings = vnMapping.mapped(id);
                } catch (IllegalArgumentException ignored) {
                    mappings = vnMapping.unmapped;
                }
            }

            Roleset rs = frames.byId(roleset);
            if (null == rs) {
                throw new IllegalArgumentException("Missing roleset in mappings: " + roleset + "\t" + mapping.newVnClass);
            }
            List<VnClass> classes = new ArrayList<>();
            for (String possibleCls : Arrays.stream(mapping.newVnClass.split("[, ]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList())) {
                VnClass vnClass = vnIndex.getById(possibleCls);
                if (null == vnClass) {
                    if (ImmutableSet.of("no entry", "no match", "no matching sense", "word not in any class", "class does not exist",
                            "can't find", "not found", "no entry (?)",
                            "The requested URL /propbank/framesets-english-aliases/light.html was not found on this server.")
                            .contains(mapping.newVnClass().trim().toLowerCase())) {
                        continue;
                    }
                    String alternatives = vnIndex.getByLemma(rs.predicate().lemma()).stream()
                            .map(vn -> vn.verbNetId().classId())
                            .sorted()
                            .collect(Collectors.joining(", "));
                    mappingLines.add(
                            mapping.originalVnClass + " -> " + mapping.newVnClass + "\t" + roleset + "\t" + alternatives);
                } else {
                    RolesetVn pair = new RolesetVn(roleset, vnClass.verbNetId().classId());
                    if (rsVNPairs.contains(pair)) {
                        log.warn("Duplicate mapping for {} -> {}", roleset, vnClass.verbNetId());
                    } else {
                        rsVNPairs.add(pair);
                        classes.add(vnClass);
                    }
                }
            }

            mappings.addAll(classes);
        }
        Collections.sort(mappingLines);
        if (printMissing) {
            mappingLines.forEach(System.out::println);
        }

        return rolesetMappings;
    }

    private enum MappingType {
        UNMAPPED,
        MAPPED,
        MAPPED_WITH_FT,

        EXISTING_SAME_CLASS,
        EXISTING_SAME_ROOT,
        EXISTING_SAME_INTEGER,
        EXISTING_SHARED_ROLE


    }

    @Data
    @Getter
    @Setter
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RolesetMapping {

        Roleset roleset;
        VnClass vnClass;

        Map<ArgNumber, RoleMapping> roleMappings = new HashMap<>();

        public RolesetMapping(Roleset roleset, VnClass vnClass) {
            this.roleset = roleset;
            this.vnClass = vnClass;
            this.roleMappings = roleset.roles().roles().stream()
                    .filter(r -> r.number().isNumber())
                    .map(role -> new RoleMapping(roleset, vnClass, role))
                    // TODO: handle modifiers
                    .collect(Collectors.toMap(r -> r.pbRole.number(), r -> r));
        }

        public RolesetMapping add(ArgNumber number, ThematicRoleType vnRole, MappingType mappingType) {
            roleMappings.get(number)
                    .mappingType(mappingType)
                    .vnRole(vnRole);
            return this;
        }

        public RolesetMapping addNote(ArgNumber number, String note) {
            roleMappings.get(number)
                    .notes().add(note);
            return this;
        }

        public Set<ThematicRoleType> usedVnRoles() {
            return roleMappings.values().stream()
                    .filter(v -> v.vnRole != ThematicRoleType.NONE)
                    .map(v -> v.vnRole)
                    .collect(Collectors.toSet());
        }

        public Set<PbRole> missingPbRoles() {
            return roleMappings.values().stream()
                    .filter(v -> v.vnRole == ThematicRoleType.NONE)
                    .map(v -> v.pbRole)
                    .sorted(Comparator.comparing(v -> v.number().ordinal()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public String toString() {
            return roleMappings.entrySet().stream()
                    .sorted(Comparator.comparing(v -> v.getKey().ordinal()))
                    .map(v -> v.getValue().toString())
                    .collect(Collectors.joining("\n"));
        }

    }

    @Data
    @Getter
    @Setter
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RoleMapping {

        Roleset roleset;
        VnClass vnClass;
        PbRole pbRole;

        ThematicRoleType vnRole = ThematicRoleType.NONE;
        MappingType mappingType = MappingType.UNMAPPED;
        Set<String> notes = new HashSet<>();

        public RoleMapping(Roleset roleset, VnClass vnClass, PbRole pbRole) {
            this.roleset = roleset;
            this.vnClass = vnClass;
            this.pbRole = pbRole;
        }

        @Override
        public String toString() {
            return String.join("\t", Arrays.asList(roleset.id(),
                    vnClass.verbNetId().classId(),
                    pbRole.number().name(),
                    vnRole.name(),
                    mappingType.name(),
                    pbRole.description(),
                    notes.size() > 0 ? "NOTES: " + String.join(", ", notes) : ""));
        }
    }

    private static List<RolesetMapping> processRolesetMappings(Map<String, Roleset> rolesetMap,
                                                               Map<String, VnMapping> mappingsByRoleset) {

        final List<RolesetMapping> flatMappings = new ArrayList<>();

        for (Map.Entry<String, VnMapping> rolesetMapping : mappingsByRoleset.entrySet()) {

            final Roleset roleset = rolesetMap.get(rolesetMapping.getKey()); // roleset from unified frame files
            if (null == roleset) {
                log.warn("Missing roleset: {}", rolesetMapping.getKey());
                continue;
            }
            final VnMapping vnMapping = rolesetMapping.getValue(); // VerbNet mappings from PB-VN spreadsheet
            final Map<VnClassId, Map<ArgNumber, ThematicRoleType>> aliases = aliases(roleset);

            // (1) handle cases with pre-existing VerbNet class
            for (Map.Entry<VnClassId, Set<VnClass>> mappingEntry : vnMapping.mapping().entrySet()) {
                VnClassId originalVnClass = mappingEntry.getKey();

                Map<ArgNumber, ThematicRoleType> preexistingMappings = aliases.getOrDefault(originalVnClass, new HashMap<>());

                List<RolesetMapping> rolesetMappings = mappingEntry.getValue().stream()
                        .map(cls -> processMapping(originalVnClass, cls, roleset, preexistingMappings))
                        .collect(Collectors.toList());

                flatMappings.addAll(rolesetMappings);
            }

            // handle cases with new VerbNet class
            for (VnClass vnClass : vnMapping.unmapped()) {
                flatMappings.add(new RolesetMapping(roleset, vnClass));
            }
        }

        return flatMappings;
    }

    private static RolesetMapping processMapping(VnClassId originalId,
                                                 VnClass mappedClass,
                                                 Roleset roleset,
                                                 Map<ArgNumber, ThematicRoleType> roleMappings) {
        RolesetMapping rolesetMapping = new RolesetMapping(roleset, mappedClass);

        VnClassId mappedId = mappedClass.verbNetId();

        Set<ThematicRoleType> validRoles = convertRoles(mappedClass.rolesIncludeInherited());

        MappingType mappingType;
        if (mappedId.classId().equalsIgnoreCase(originalId.classId())) {
            mappingType = MappingType.EXISTING_SAME_CLASS;
        } else if (mappedId.rootId().equalsIgnoreCase(originalId.rootId())) {
            mappingType = MappingType.EXISTING_SAME_ROOT;
        } else if (mappedId.number().equals(originalId.number())) {
            mappingType = MappingType.EXISTING_SAME_INTEGER;
        } else {
            mappingType = MappingType.EXISTING_SHARED_ROLE;
            return rolesetMapping;
        }

        // iterate over PropBank roles, applying pre-existing mappings
        for (PbRole role : roleset.roles().roles()) {
            // TODO: handle modifiers
            if (role.number().isModifier()) {
                continue;
            }
            ThematicRoleType thematicRoleType = roleMappings.get(role.number());
            if (null == thematicRoleType) {
                // unmapped, add to missing
                continue;
            }
            if (validRoles.contains(thematicRoleType)) {
                // mapped role exists, create mapping
                rolesetMapping.add(role.number(), thematicRoleType, mappingType);
            } else {
                rolesetMapping.addNote(role.number(), "Original mapping: " + thematicRoleType.name());
                // unmapped, add to missing (maybe add to notes?)
            }
        }
        return rolesetMapping;
    }

    private static Set<ThematicRoleType> convertRoles(List<VnThematicRole> roles) {
        return roles.stream()
                .map(r -> ThematicRoleType.fromString(r.type()).orElse(ThematicRoleType.NONE))
                .filter(r -> r != ThematicRoleType.NONE)
                .collect(Collectors.toSet());
    }

    private static Map<VnClassId, Map<ArgNumber, ThematicRoleType>> aliases(Roleset roleset) {
        Map<VnClassId, Map<ArgNumber, ThematicRoleType>> aliases = new HashMap<>();
        for (PbRole role : roleset.roles().roles()) {
            for (VerbNetRole verbNetRole : role.verbNetRoles()) {
                try {
                    VnClassId id = VnClassId.parse(verbNetRole.verbNetClass());
                    Map<ArgNumber, ThematicRoleType> roleMap = aliases.computeIfAbsent(id, r -> new HashMap<>());
                    ThematicRoleType type = ThematicRoleType.fromString(verbNetRole.thematicRole()).orElse(ThematicRoleType.NONE);
                    if (type != ThematicRoleType.NONE) {
                        roleMap.put(role.number(), type);
                    } else {
                        log.warn("\"{}\" not found for {} mapping to {}", verbNetRole.thematicRole(), roleset.id(),
                                verbNetRole.verbNetClass());
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid id for {} mapping {} to {}.{}", verbNetRole.verbNetClass(),
                            verbNetRole.thematicRole(),
                            roleset.id(), role.number());
                }
            }
        }
        return aliases;
    }

    private static void applyHeuristicMappings(List<RolesetMapping> mappings,
                                               Map<FunctionTag, List<ThematicRoleType>> ftThemRoleMappings) {
        for (RolesetMapping mapping : mappings) {
            VnClass vnClass = mapping.vnClass();
            Set<ThematicRoleType> validRoles = convertRoles(vnClass.rolesIncludeInherited());

            for (PbRole role : mapping.missingPbRoles()) {
                FunctionTag ft = role.functionTag();

                List<ThematicRoleType> plausibleMappings = ftThemRoleMappings.getOrDefault(ft, Collections.emptyList()).stream()
                        .filter(validRoles::contains)
                        .filter(r -> !mapping.usedVnRoles().contains(r))
                        .collect(Collectors.toList());
                if (plausibleMappings.size() > 0) {
                    mapping.add(role.number(), plausibleMappings.get(0), MappingType.MAPPED_WITH_FT);
                    if (plausibleMappings.size() > 1) {
                        mapping.addNote(role.number(), "Other possible mappings: " + plausibleMappings.subList(1,
                                plausibleMappings.size()).stream().map(Enum::name).collect(Collectors.joining(" | ")));
                    }
                }
            }
        }
    }

    private static void addPossibleRoles(List<RolesetMapping> mappings) {
        for (RolesetMapping mapping : mappings) {
            VnClass vnClass = mapping.vnClass();
            Set<ThematicRoleType> validRoles = convertRoles(vnClass.rolesIncludeInherited());

            for (PbRole role : mapping.missingPbRoles()) {
                String possible = validRoles.stream().filter(r -> !mapping.usedVnRoles().contains(r))
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
                if (possible.length() > 0) {
                    mapping.addNote(role.number(), "remaining=[" + possible + "]");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String framesPath = "data/unified-frames.bin";
        String mappingsPath = args[0];

        VnIndex vnIndex = new DefaultVnIndex();
        List<Frameset> framesets = deserializeFrames(new ByteArrayInputStream(Files.readAllBytes(Paths.get(framesPath))));
        Map<String, Roleset> rolesetMap = framesets.stream()
                .flatMap(f -> f.rolesets().stream())
                .collect(Collectors.toMap(Roleset::id, Function.identity()));

        Map<String, VnMapping> mappings = getMappings(framesets, vnIndex, mappingsPath, false);

        Map<FunctionTag, List<ThematicRoleType>> ftThemRoleMappings = ImmutableMap.<FunctionTag, List<ThematicRoleType>>builder()
                .put(FunctionTag.PAG, Arrays.asList(ThematicRoleType.AGENT, ThematicRoleType.STIMULUS, ThematicRoleType.CAUSER,
                        ThematicRoleType.PIVOT))
                .put(FunctionTag.PPT, Arrays.asList(ThematicRoleType.THEME, ThematicRoleType.PATIENT, ThematicRoleType.EXPERIENCER,
                        ThematicRoleType.TOPIC, ThematicRoleType.CO_THEME, ThematicRoleType.REFLEXIVE))
                .put(FunctionTag.DIR, Arrays.asList(ThematicRoleType.INITIAL_LOCATION, ThematicRoleType.SOURCE))
                .put(FunctionTag.LOC, Collections.singletonList(ThematicRoleType.LOCATION))
                .put(FunctionTag.EXT, Collections.singletonList(ThematicRoleType.EXTENT))
                .put(FunctionTag.GOL, Arrays.asList(ThematicRoleType.DESTINATION, ThematicRoleType.GOAL,
                        ThematicRoleType.BENEFICIARY, ThematicRoleType.RECIPIENT))
                .put(FunctionTag.PRD, Arrays.asList(ThematicRoleType.ATTRIBUTE, ThematicRoleType.PRODUCT,
                        ThematicRoleType.PREDICATE))
                .put(FunctionTag.MNR, Collections.singletonList(ThematicRoleType.INSTRUMENT))
                .build();

        List<RolesetMapping> rolesetMappings = processRolesetMappings(rolesetMap, mappings);
        applyHeuristicMappings(rolesetMappings, ftThemRoleMappings);
        addPossibleRoles(rolesetMappings);
        try (PrintWriter printWriter = new PrintWriter(new File(new File(mappingsPath).getParentFile(), "role-mappings.tsv"))) {
            printWriter.println(String.join("\t", Arrays.asList("PB", "VN", "PB Role", "VN Role", "Mapping Type",
                    "PB Description", "Notes")));
            rolesetMappings.forEach(printWriter::println);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
