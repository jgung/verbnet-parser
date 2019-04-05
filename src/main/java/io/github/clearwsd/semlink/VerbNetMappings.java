package io.github.clearwsd.semlink;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.propbank.frames.Frameset;
import io.github.clearwsd.propbank.frames.Predicate;
import io.github.clearwsd.propbank.frames.Role;
import io.github.clearwsd.propbank.frames.Roleset;
import io.github.clearwsd.propbank.frames.RolesetAlias;
import io.github.clearwsd.propbank.frames.VerbNetRole;
import io.github.clearwsd.semlink.PbVnMapping.MappedRole;
import io.github.clearwsd.semlink.PbVnMapping.RolesMapping;
import io.github.clearwsd.semlink.PbVnMapping.RolesetMapping;
import io.github.clearwsd.util.TsvUtils;
import io.github.clearwsd.verbnet.VerbNet;
import io.github.clearwsd.verbnet.VerbNetClass;
import lombok.extern.slf4j.Slf4j;

import static io.github.clearwsd.propbank.frames.FramesetFactory.deserializeFrames;

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

    private static RolesetMapping getRolesetMappings(Roleset roleset) {
        RolesetMapping rsMapping = new RolesetMapping().id(roleset.id());

        Multimap<String, MappedRole> vnclsRoleMap = LinkedHashMultimap.create();
        for (Role role : roleset.roles().roles()) {
            for (VerbNetRole vnRole : role.verbNetRoles()) {
                vnclsRoleMap.put(vnRole.verbNetClass(),
                        new MappedRole()
                                .number(role.number())
                                .vntheta(vnRole.thematicRole()));
            }
        }

        for (Map.Entry<String, Collection<MappedRole>> entry : vnclsRoleMap.asMap().entrySet()) {
            RolesMapping rolesMapping = new RolesMapping()
                    .vncls(entry.getKey())
                    .roles(new ArrayList<>(entry.getValue()));
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
                    RolesetMapping rsMapping = getRolesetMappings(roleset);
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
        VerbNet verbNet = new VerbNet();

        PbVnMappings mappings = new PbVnMappings(result);

        List<String> results = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<PbVnMappings.Roleset>>> lemma : mappings.lemmaClassRolesetMap().entrySet()) {

            for (Map.Entry<String, List<PbVnMappings.Roleset>> vncls : lemma.getValue().entrySet()) {

                for (PbVnMappings.Roleset rs : vncls.getValue()) {

                    Map<String, String> vnMappings = roleset2Class.getOrDefault(rs.id(), Collections.emptyMap());
                    String corrected = vnMappings.get(vncls.getKey());
                    String clsId = vncls.getKey();
                    if (null != corrected && verbNet.byId(corrected).isPresent()) {
                        clsId = corrected;
                    }


                    Optional<VerbNetClass> id = verbNet.byId(clsId);
                    if (!id.isPresent()) {
                        List<VerbNetClass> partialMatches = verbNet.byBaseIdAndLemma(clsId, lemma.getKey());
                        if (partialMatches.size() > 0) {
                            results.add(String.format("%s\t%s\t%s\t%s\t%s",
                                    clsId, vncls.getKey(), rs.id(), "Partial match", partialMatches.stream()
                                            .map(cls -> cls.id().classId())
                                            .distinct()
                                            .sorted()
                                            .collect(Collectors.joining(", "))));
                        } else {
                            results.add(String.format("%s\t%s\t%s\t%s\t%s",
                                    clsId, vncls.getKey(), rs.id(), "Missing class",
                                    verbNet.byLemma(lemma.getKey()).stream()
                                            .map(cls -> cls.id().classId())
                                            .distinct()
                                            .sorted()
                                            .collect(Collectors.joining(", "))));
                        }
                    }

                    final String mappedId = clsId;
                    id.ifPresent(vid -> {
                        List<VerbNetClass> verbNetClasses = verbNet.byLemma(lemma.getKey());
                        if (!verbNetClasses.contains(vid)) {
                            boolean found = false;
                            for (VerbNetClass cls : verbNetClasses) {
                                if (cls.relatedClasses().contains(vid)) {
                                    found = true;
                                }
                            }
                            if (!found) {
                                results.add(String.format("%s\t%s\t%s\t%s\t%s",
                                        mappedId, vncls.getKey(), rs.id(), "Missing lemma", ""));
                            }
                        } else {

                            Map<String, String> roleFixes = role2Role.getOrDefault(vid.id().classId(), Collections.emptyMap());

                            Set<String> mappedRoles = rs.roleMappings().values().stream()
                                    .flatMap(Collection::stream)
                                    .map(String::toLowerCase)
                                    .map(r -> roleFixes.getOrDefault(r, r))
                                    .collect(Collectors.toSet());

                            Set<String> roles =
                                    vid.parentClasses().stream().map(r -> r.verbClass().getThematicRoles().stream()
                                            .map(role -> role.getType().getID().toLowerCase())
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
                    });
                }
            }
        }
        System.out.println("Mapped ID\tID\tRoleset\tType\tPossible Mappings\tCorrections\tMissing Roles");
        results.stream().sorted().forEach(System.out::println);
    }

    public static void main(String[] args) throws IOException {
        String framesPath = "src/main/resources/propbank-frames.bin";
        String outPath = "src/main/resources/pbvn-mappings.json";
        writeMappings(framesPath, outPath);
        incompleteMappings(outPath);
    }

}
