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

package io.github.semlink.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.github.semlink.util.StringUtils;
import lombok.NonNull;

/**
 * Default {@link SentenceNormalizer} implementation.
 *
 * @author jgung
 */
public class DefaultSentenceNormalizer implements SentenceNormalizer {

    private Pattern punctuationPattern = Pattern.compile("\\p{Punct}$");
    private Pattern questionPattern = Pattern.compile(
            "^(can|could|do|does|how|may|might|must|should|to whom|was|what|when|where|which|who|why|will|would)\\b",
            Pattern.CASE_INSENSITIVE);

    private Map<Pattern, String> contractions = new LinkedHashMap<Pattern, String>() {
        {
            put(Pattern.compile("(is|are|was|were|have|has|had|does|did|do|ca|could|might|must|should|would|need) ?n'?t\\b",
                    Pattern.CASE_INSENSITIVE), "$1 not");
            put(Pattern.compile("(\\S+) ?'ve\\b", Pattern.CASE_INSENSITIVE), "$1 have");
            put(Pattern.compile("(\\S+) ?'re\\b", Pattern.CASE_INSENSITIVE), "$1 are");
            put(Pattern.compile("(\\S+) ?'ll\\b", Pattern.CASE_INSENSITIVE), "$1 will");
            put(Pattern.compile("(\\S+) ?'d\\b", Pattern.CASE_INSENSITIVE), "$1 would");
            put(Pattern.compile("(\\S+) ?'m\\b", Pattern.CASE_INSENSITIVE), "$1 am");
            put(Pattern.compile("\\bcannot\\b", Pattern.CASE_INSENSITIVE), "can not");
            put(Pattern.compile("\\bwanna\\b", Pattern.CASE_INSENSITIVE), "want to");
            put(Pattern.compile("\\blemme\\b", Pattern.CASE_INSENSITIVE), "let me");
            put(Pattern.compile("\\bgotta\\b", Pattern.CASE_INSENSITIVE), "got to");
            put(Pattern.compile("\\bgonna\\b", Pattern.CASE_INSENSITIVE), "going to");
            put(Pattern.compile("\\bdunno\\b", Pattern.CASE_INSENSITIVE), "do not know");
            put(Pattern.compile("\\bgimme\\b", Pattern.CASE_INSENSITIVE), "give me");
            put(Pattern.compile("\\bwo\\b(?=not)", Pattern.CASE_INSENSITIVE), "will");
            put(Pattern.compile("\\bca\\b(?=not)", Pattern.CASE_INSENSITIVE), "can");
        }
    };

    @Override
    public String normalize(@NonNull String sentence) {
        String result = sentence.trim();
        if (sentence.isEmpty()) {
            return sentence;
        }
        result = StringUtils.capitalize(result);

        boolean hasPunctuation = punctuationPattern.matcher(result).find();
        boolean possibleQuestion = questionPattern.matcher(result).find();

        if (!hasPunctuation) {
            result = result + (possibleQuestion ? "?" : ".");
        }

        result = normalizeContractions(result);

        return result;
    }

    private String normalizeContractions(String sentence) {
        for (Map.Entry<Pattern, String> entry : contractions.entrySet()) {
            sentence = entry.getKey().matcher(sentence).replaceAll(entry.getValue());
        }
        return sentence;
    }

}
