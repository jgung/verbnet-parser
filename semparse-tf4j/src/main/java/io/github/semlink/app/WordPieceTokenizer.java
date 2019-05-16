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

package io.github.semlink.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * WordPiece tokenizer to produce inputs to BERT published models. Based on BERT implementation.
 *
 * @author jamesgung
 * @see <a href="https://github.com/google-research/bert">https://github.com/google-research/bert</a>
 * @see <a href="https://arxiv.org/abs/1609.08144">https://arxiv.org/abs/1609.08144</a>
 */
@Slf4j
@AllArgsConstructor
public class WordPieceTokenizer {

    public static List<String> whitespaceTokenize(@NonNull String text) {
        text = text.trim();
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(text.split("\\s+"));
    }

    /**
     * Read a wordpiece vocabulary from a given path, first checking classpath resources, then the file system.
     *
     * @param path vocabulary path
     * @return wordpiece vocabulary
     */
    public static Map<String, Integer> readVocabulary(@NonNull String path) {
        try {
            Map<String, Integer> vocab = new HashMap<>();
            URL resource = WordPieceTokenizer.class.getClassLoader().getResource(path);
            try (InputStream inputStream = null == resource ? new FileInputStream(path) : resource.openStream()) {
                new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset())).lines()
                    .filter(line -> !line.isEmpty())
                    .forEach(entry -> vocab.put(entry, vocab.size()));
            }
            log.info("Read {}-length vocabulary from {}", vocab.size(), path);
            return vocab;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load vocabulary at path: " + path, e);
        }
    }

    private Map<String, Integer> vocabulary;
    private String unkToken;
    private int maxInputCharsPerWord;

    public WordPieceTokenizer(Map<String, Integer> vocabulary) {
        this(vocabulary, "[UNK]", 200);
    }

    public WordPieceTokenizer(@NonNull String path) {
        this(readVocabulary(path));
    }

    /**
     * Convert a list of word piece tokens to corresponding vocabulary IDs.
     *
     * @param tokens word piece tokens
     * @return vocabulary indices
     */
    public List<Integer> convertTokensToIds(@NonNull List<String> tokens) {
        return tokens.stream()
            .map(vocabulary::get)
            .collect(Collectors.toList());
    }

    /**
     * Tokenize input text into a list of word pieces based on the vocabulary.
     *
     * @param text input text, a single token or whitespace separated sentence
     * @return list of word pieces
     */
    public List<String> tokenize(@NonNull String text) {
        List<String> outputTokens = new ArrayList<>();

        for (String token : whitespaceTokenize(text)) {

            if (token.length() > maxInputCharsPerWord) {
                outputTokens.add(unkToken);
                continue;
            }

            boolean isBad = false;
            int start = 0;
            List<String> subTokens = new ArrayList<>();
            while (start < token.length()) {
                int end = token.length();
                String curSubstr = null;

                while (start < end) {
                    String substr = token.substring(start, end);
                    if (start > 0) {
                        substr = "##" + substr;
                    }
                    if (vocabulary.containsKey(substr)) {
                        curSubstr = substr;
                        break;
                    }
                    end -= 1;
                }
                if (null == curSubstr) {
                    isBad = true;
                    break;
                }
                subTokens.add(curSubstr);
                start = end;
            }

            if (isBad) {
                outputTokens.add(unkToken);
            } else {
                outputTokens.addAll(subTokens);
            }

        }
        return outputTokens;
    }

}
