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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.github.semlink.verbnet.DefaultVnIndex;
import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.VnIndex;
import io.github.semlink.verbnet.VnMember;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility to collect and output VN-PB mappings from VerbNet classes.
 *
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WriteVerbNetMappings {

    public static Map<String, Collection<String>> pb2VerbNet() {
        Multimap<String, String> pb2VerbNet = LinkedListMultimap.create();
        VnIndex index = new DefaultVnIndex();
        for (VnClass root : index.roots()) {
            for (VnClass cls : root.related()) {
                for (VnMember member : cls.members()) {
                    for (String roleset : member.groupings()) {
                        if (roleset.isEmpty()) {
                            continue;
                        }
                        pb2VerbNet.put(roleset, cls.verbNetId().rootId());
                    }
                }
            }
        }
        return pb2VerbNet.asMap();
    }

    public static Map<String, Collection<String>> members() {
        Multimap<String, String> members = LinkedListMultimap.create();
        VnIndex index = new DefaultVnIndex();
        for (VnClass root : index.roots()) {
            for (VnClass cls : root.related()) {
                for (VnMember member : cls.members()) {
                    members.put(root.verbNetId().rootId(), member.name());
                }
            }
        }
        return members.asMap();
    }

    public static void main(String[] args) throws IOException {
        String outputDirectory = args.length > 0 ? args[0] : ".";

        ObjectWriter om = new ObjectMapper().writerWithDefaultPrettyPrinter();
        Map<String, List<String>> pb2VerbNet = new TreeMap<>(pb2VerbNet().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList()))));
        om.writeValue(new File(outputDirectory, "pb2vn.json"), pb2VerbNet);

        Map<String, List<String>> memberMap = new TreeMap<>(members().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList()))));
        om.writeValue(new File(outputDirectory, "vncls-members.json"), memberMap);
    }

}
