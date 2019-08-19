package io.github.semlink.clearwsd;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;

/**
 * Simple WordNet-based lemmatizer.
 *
 * @author jamesgung
 */
@Slf4j
@AllArgsConstructor
public class WnLemmatizer {

    private MorphologicalProcessor processor;
    private Map<String, String> mappings;

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
