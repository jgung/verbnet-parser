package io.github.semlink.extractor;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Feature vocabulary.
 *
 * @author jgung
 */
@Slf4j
public class Vocabulary {

    public static final String PAD_WORD = "<PAD>";
    public static final String UNKNOWN_WORD = "<UNK>";
    public static final String START_WORD = "<BOS>";
    public static final String END_WORD = "<EOS>";

    private BiMap<String, Integer> featureIndexMap;
    private int oov;

    public Vocabulary(Map<String, Integer> featureIndexMap, String oov) {
        this.featureIndexMap = ImmutableBiMap.copyOf(featureIndexMap);
        if (!this.featureIndexMap.containsKey(oov)) {
            log.warn("OOV feature \"{}\" not found in vocabulary.", oov);
            this.oov = 0;
        } else {
            this.oov = this.featureIndexMap.get(oov);
        }
    }

    public int featToIndex(String feature) {
        return featureIndexMap.getOrDefault(feature, oov);
    }

    public String indexToFeat(int index) {
        return featureIndexMap.inverse().get(index);
    }

    public static Vocabulary read(InputStream inputStream, String oov) {
        Map<String, Integer> featureIndexMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (featureIndexMap.containsKey(line)) {
                    throw new IllegalStateException("Duplicate key found in vocabulary: " + line);
                }
                featureIndexMap.put(line, featureIndexMap.size());
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load feature vocabulary", e);
        }
        return new Vocabulary(featureIndexMap, oov);
    }

    public static Vocabulary read(String path, String oov) {
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            return Vocabulary.read(fileInputStream, oov);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load feature vocabulary from file at " + path, e);
        }
    }

}
