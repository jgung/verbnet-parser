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

package io.github.semlink.propbank;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.semlink.propbank.frames.Frameset;
import io.github.semlink.propbank.frames.Predicate;
import io.github.semlink.propbank.frames.Roleset;
import io.github.semlink.propbank.frames.RolesetAlias;
import lombok.NonNull;

import static io.github.semlink.propbank.frames.FramesetFactory.deserializeFrames;
import static io.github.semlink.propbank.frames.FramesetFactory.readFramesets;

/**
 * Default {@link PbIndex} implementation.
 *
 * @author jgung
 */
public class DefaultPbIndex implements PbIndex {

    private Map<String, Roleset> rolesById = new HashMap<>();
    private ListMultimap<String, Roleset> rolesByLemma = LinkedListMultimap.create();

    /**
     * Initialize from a Java-serialized binary containing PropBank frames.
     */
    public DefaultPbIndex(@NonNull InputStream pbBinInputStream) {
        init(deserializeFrames(pbBinInputStream));
    }

    /**
     * Initialize from a path pointing to a directory containing frame files.
     */
    public DefaultPbIndex(@NonNull Path framesDirPath) {
        init(readFramesets(framesDirPath));
    }

    /**
     * Initialize from a list of {@link Frameset frames}.
     */
    public DefaultPbIndex(@NonNull List<Frameset> frames) {
        init(frames);
    }

    private void init(@NonNull List<Frameset> frames) {
        for (Frameset frameset : frames) {
            for (Predicate predicate : frameset.predicates()) {
                for (Roleset roleset : predicate.rolesets()) {
                    rolesById.put(roleset.id(), roleset);
                    if (roleset.aliases().isEmpty()) {
                        rolesByLemma.put(predicate.lemma(), roleset);
                    } else {
                        for (RolesetAlias alias : roleset.aliases()) {
                            rolesByLemma.put(alias.lemma(), roleset);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Roleset getById(@NonNull String rolesetId) {
        return rolesById.get(rolesetId);
    }

    @Override
    public List<Roleset> getByLemma(@NonNull String lemma) {
        return rolesByLemma.get(lemma);
    }

    public static DefaultPbIndex fromBinary(String path) {
        try {
            URL url = DefaultPbIndex.class.getClassLoader().getResource("propbank/unified-frames.bin");
            if (null == url) {
                try (FileInputStream fis = new FileInputStream(path)) {
                    return new DefaultPbIndex(fis);
                }
            }
            try (InputStream inputStream = url.openStream()) {
                return new DefaultPbIndex(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
