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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * WordPiece tokenizer unit tests.
 *
 * @author jamesgung
 */
public class WordPieceTokenizerTest {

    private static Map<String, Integer> testVocabulary() {
        Map<String, Integer> vocab = new HashMap<>();
        Arrays.asList("[UNK]", "[CLS]", "[SEP]", "want", "##want", "##ed", "wa", "un", "runn", "##ing", ",")
            .forEach(entry -> vocab.put(entry, vocab.size()));
        return vocab;
    }

    @Test
    public void readVocabulary() {
        Map<String, Integer> vocab = WordPieceTokenizer.readVocabulary("wordpiece/bert-base.cased.txt");
        assertEquals(28996, vocab.size());
        assertEquals(101, (long) vocab.get("[CLS]"));
    }

    @Test
    public void tokenize() {
        WordPieceTokenizer tokenizer = new WordPieceTokenizer(testVocabulary());

        assertEquals(Collections.emptyList(), tokenizer.tokenize(""));

        assertEquals(Arrays.asList("un", "##want", "##ed", ",", "runn", "##ing"),
            tokenizer.tokenize("unwanted , running"));

        assertEquals(Arrays.asList("[UNK]", "runn", "##ing"), tokenizer.tokenize("unwantedx running"));
    }

    @Test
    public void convertTokensToIds() {
        WordPieceTokenizer tokenizer = new WordPieceTokenizer(testVocabulary());
        assertEquals(Arrays.asList(7, 4, 5, 8, 9),
            tokenizer.convertTokensToIds(Arrays.asList("un", "##want", "##ed", "runn", "##ing")));
    }

}