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

package io.github.semlink.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * TSV utilities.
 *
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsvUtils {

    public static Map<String, Map<String, String>> tsv2Map(String path, int key1Col, int key2Col, int valCol) throws IOException {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (String line : Files.readAllLines(Paths.get(path), Charset.defaultCharset())) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split("\t");
            if (fields.length < 3) {
                continue;
            }
            Map<String, String> subMap = result.computeIfAbsent(fields[key1Col], ignored -> new HashMap<>());
            subMap.put(fields[key2Col], fields[valCol]);
        }
        return result;
    }

}
