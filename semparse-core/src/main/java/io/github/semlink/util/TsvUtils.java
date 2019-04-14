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
