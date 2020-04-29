/*
 * Copyright 2020 James Gung
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

package io.github.semlink.clearwsd;

import com.google.common.collect.ImmutableMap;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple WordNet-based lemmatizer.
 *
 * @author jamesgung
 */
@Slf4j
@AllArgsConstructor
public class WnLemmatizer {

    private final MorphologicalProcessor processor;
    private final Map<String, String> mappings;

    public WnLemmatizer() {
        try {
            Dictionary dictionary = Dictionary.getDefaultResourceInstance();
            processor = dictionary.getMorphologicalProcessor();
            mappings = getMappings();
        } catch (JWNLException e) {
            throw new RuntimeException("Unable to load WordNet", e);
        }
    }

    public String lemmatize(String word, String pos) {
        POS wnPos = getPos(pos);
        if (wnPos != null && !pos.toUpperCase().startsWith("NNP")) {
            try {
                IndexWord indexWord = processor.lookupBaseForm(wnPos, word);
                if (null != indexWord) {
                    return indexWord.getLemma();
                }
            } catch (JWNLException e) {
                log.warn("Error lemmatizing word: {}", word, e);
            }
        }
        word = word.toLowerCase();
        if (mappings.containsKey(word)) {
            return mappings.get(word);
        }
        return word.replaceAll("\\d+", "0");
    }

    private POS getPos(String penn) {
        penn = penn.toUpperCase();
        if (penn.startsWith("N")) {
            return POS.NOUN;
        } else if (penn.startsWith("V")) {
            return POS.VERB;
        } else if (penn.startsWith("J")) {
            return POS.ADJECTIVE;
        } else if (penn.startsWith("R")) {
            return POS.ADVERB;
        }
        return null;
    }

    private Map<String, String> getMappings() {
        return ImmutableMap.of("n't", "not");
    }

}
