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

package io.github.semlink.parser.feat;

import static io.github.semlink.tensor.TensorflowFeatureUtils.int64Feature;
import static io.github.semlink.tensor.TensorflowFeatureUtils.int64Features;

import io.github.semlink.app.WordPieceTokenizer;
import io.github.semlink.type.HasFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.Setter;
import org.tensorflow.example.FeatureLists;
import org.tensorflow.example.Features;
import org.tensorflow.example.SequenceExample;

/**
 * Sequence example extractor that uses a {@link WordPieceTokenizer} to convert the input prior to normal processing/extraction.
 *
 * @author jamesgung
 */
public class BertDepExampleExtractor extends BertExampleExtractor {

    public static final String BERT_CLS = "[CLS]";
    public static final String BERT_SEP = "[SEP]";

    /**
     * Placeholder for number used for tracking instance numbers during evaluation.
     */
    @Setter
    private String sentenceIndexKey = "sentence_idx";

    public BertDepExampleExtractor(@NonNull WordPieceTokenizer wordPieceTokenizer) {
        super(wordPieceTokenizer);
    }

    @Override
    public SequenceExample extractSequence(@NonNull HasFields sequence) {
        List<String> words = sequence.field(wordsKey);

        List<String> splitTokens = new ArrayList<>();
        List<Integer> maskValues = new ArrayList<>();

        // [CLS], word_1, word_2, ...
        splitTokens.add(BERT_CLS);
        maskValues.add(0);

        for (String token : words) {
            List<String> subtokens = wordPieceTokenizer.tokenize(token);

            splitTokens.addAll(subtokens);
            maskValues.add(1);
            if (subtokens.size() > 0) {
                maskValues.addAll(Collections.nCopies(subtokens.size() - 1, 0));
            }
        }

        // ..., word_n-1, word_n, [SEP]
        splitTokens.add(BERT_SEP);
        maskValues.add(0);

        FeatureLists.Builder featureLists = FeatureLists.newBuilder()
                // IDs for WordPiece tokens
                .putFeatureList(bertIdsKey, int64Features(wordPieceTokenizer.convertTokensToIds(splitTokens)))
                // mask used to ignore subtokens in prediction
                .putFeatureList(maskKey, int64Features(maskValues));

        Features.Builder features = Features.newBuilder()
                // boiler plate
                .putFeature(lengthKey, int64Feature(words.size()))
                .putFeature(bertLengthKey, int64Feature(splitTokens.size()))
                .putFeature(sentenceIndexKey, int64Feature(0));

        return SequenceExample.newBuilder()
                .setContext(features)
                .setFeatureLists(featureLists)
                .build();
    }

}
