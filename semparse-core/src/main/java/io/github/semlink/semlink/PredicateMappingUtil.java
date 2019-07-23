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

import com.google.common.base.Strings;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.semlink.propbank.frames.Frameset;
import io.github.semlink.propbank.frames.PbRole;
import io.github.semlink.propbank.frames.Roleset;
import io.github.semlink.propbank.frames.RolesetAlias;
import io.github.semlink.propbank.frames.VerbNetRole;
import io.github.semlink.verbnet.DefaultVnIndex;
import io.github.semlink.verbnet.VnIndex;
import lombok.experimental.UtilityClass;

import static io.github.semlink.propbank.frames.FramesetFactory.deserializeFrames;

/**
 * Utilities used to write nominal predicate mappings to VerbNet classes to a file.
 *
 * @author jgung
 */
@UtilityClass
public class PredicateMappingUtil {

    /**
     * Write mappings from nouns to VerbNet classes ordered based on frequency of roleset and VerbNet class.
     *
     * @param rsCounts   PB roleset counts by lemma
     * @param vnCounts   VN class counts by lemma
     * @param frames     frames
     * @param outputPath output path
     */
    public static void writeNominalPredicateMappings(Map<String, Map<String, Integer>> rsCounts,
                                                     Map<String, Map<String, Integer>> vnCounts,
                                                     List<Frameset> frames,
                                                     String outputPath) throws FileNotFoundException {
        List<String> fields = new ArrayList<>();
        VnIndex vnIndex = new DefaultVnIndex();
        for (Frameset frameset : frames) {
            for (Roleset roleset : frameset.rolesets()) {
                String predicateLemma = roleset.predicate().lemma();
                Map<String, Integer> allCounts = rsCounts.getOrDefault(predicateLemma, new HashMap<>());
                Map<String, Integer> verbNetCounts = vnCounts.getOrDefault(predicateLemma, new HashMap<>());

                Optional<RolesetAlias> verbAlias = roleset.aliases().stream()
                        .filter(a -> a.pos() == RolesetAlias.AliasPos.V)
                        .filter(a -> !Strings.isNullOrEmpty(a.verbnet()))
                        .findFirst();
                String verbNetMappings;
                String lemma;
                if (!verbAlias.isPresent()) {
                    List<String> roles = roleset.roles().roles().stream()
                            .map(PbRole::verbNetRoles)
                            .flatMap(List::stream)
                            .map(VerbNetRole::verbNetClass)
                            .filter(s -> !Strings.isNullOrEmpty(s))
                            .distinct()
                            .filter(v -> vnIndex.getById(v) != null)
                            .sorted(Comparator.comparing(s -> -verbNetCounts.getOrDefault(s, 0)))
                            .collect(Collectors.toList());
                    if (roles.isEmpty()) {
                        continue;
                    }
                    verbNetMappings = String.join(" ", roles);
                    lemma = roleset.aliases().stream()
                            .filter(a -> a.pos() == RolesetAlias.AliasPos.V)
                            .filter(a -> !Strings.isNullOrEmpty(a.verbnet()))
                            .map(RolesetAlias::lemma)
                            .findFirst().orElse(roleset.id().substring(0, roleset.id().length() - 3));
                } else {
                    verbNetMappings = verbAlias.get().verbnet();
                    lemma = verbAlias.get().lemma();
                }
                if (verbNetMappings.length() < 2) {
                    continue;
                }
                for (RolesetAlias nominal : roleset.aliases().stream()
                        .filter(a -> a.pos() == RolesetAlias.AliasPos.N)
                        .collect(Collectors.toList())) {
                    if (allCounts.getOrDefault(roleset.id(), 0)
                            == allCounts.values().stream().mapToInt(i -> i).max().orElse(0)) {
                        fields.add(nominal.lemma() + "\t" + lemma + "\t" + verbNetMappings
                                + "\t" + allCounts.getOrDefault(roleset.id(), 0));
                    }
                }
            }
        }
        Collections.sort(fields);

        try (PrintWriter printWriter = new PrintWriter(outputPath)) {
            fields.forEach(printWriter::println);
        }
    }

    /**
     * Write a JSON file with most frequent senses for rolesets from a CoNLL-2012-formatted file.
     * <p/>
     * <code>
     * {
     * "cancel": {
     * "cancel.01": 11
     * },
     * "prepare": {
     * "prepare.01": 4,
     * "prepare.02": 21
     * },
     * ...
     * </code>
     *
     * @param trainingPath CoNLL SRL input path
     * @return roleset frequencies by lemma
     */
    public static Map<String, Map<String, Integer>> pbMostFrequent(String trainingPath) throws IOException {
        Map<String, Map<String, Integer>> rolesetCounts = new HashMap<>();
        try (Stream<String> lines = Files.lines(Paths.get(trainingPath))) {
            lines.forEach(line -> {
                if (line.trim().isEmpty()) {
                    return;
                }
                String[] fields = line.split("\\s+");
                if (fields.length < 8) {
                    return;
                }
                String lemma = fields[6];
                String roleset = fields[7];
                if (lemma.equals("-") || roleset.equals("-")) {
                    return;
                }
                String rolesetId = lemma + "." + roleset;
                Map<String, Integer> counts = rolesetCounts.computeIfAbsent(lemma, l -> new HashMap<>());
                counts.put(rolesetId, counts.getOrDefault(rolesetId, 0) + 1);
            });
        }
        return rolesetCounts;
    }

    /**
     * Write a JSON file with most frequent senses for VerbNet classes from a SemLink file.
     * <p/>
     * <code>
     * {
     * "recline": {
     * "47.6": 3
     * },
     * "prepare": {
     * "55.5-1": 37,
     * "26.3-1": 40
     * },
     * ...
     * </code>
     *
     * @param trainingPath CoNLL SRL input path
     * @return VN class frequencies by lemma
     */
    public static Map<String, Map<String, Integer>> vnMostFrequent(String trainingPath) throws IOException {
        Map<String, Map<String, Integer>> senseCounts = new HashMap<>();
        try (Stream<String> lines = Files.lines(Paths.get(trainingPath))) {
            lines.forEach(line -> {
                if (line.trim().isEmpty()) {
                    return;
                }
                String[] fields = line.split("\\s+");
                if (fields.length < 5) {
                    return;
                }
                String lemma = fields[3];
                String sense = fields[4];
                if (sense.equals("None")) {
                    return;
                }
                Map<String, Integer> counts = senseCounts.computeIfAbsent(lemma, l -> new HashMap<>());
                counts.put(sense, counts.getOrDefault(sense, 0) + 1);
            });
        }
        return senseCounts;
    }

    public static void main(String[] args) throws IOException {
        String framesPath = args[0];
        String pbTrainPath = args[1];
        String vnTrainPath = args[2];
        String outputPath = args[3];
        Map<String, Map<String, Integer>> pbCounts = pbMostFrequent(pbTrainPath);
        Map<String, Map<String, Integer>> vnCounts = vnMostFrequent(vnTrainPath);
        List<Frameset> frames = deserializeFrames(new ByteArrayInputStream(Files.readAllBytes(Paths.get(framesPath))));

        writeNominalPredicateMappings(pbCounts, vnCounts, frames, outputPath);

    }

}
