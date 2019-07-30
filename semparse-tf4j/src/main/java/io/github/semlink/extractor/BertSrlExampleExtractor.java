package io.github.semlink.extractor;

import org.tensorflow.example.FeatureLists;
import org.tensorflow.example.Features;
import org.tensorflow.example.SequenceExample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.semlink.app.WordPieceTokenizer;
import io.github.semlink.type.HasFields;
import lombok.NonNull;
import lombok.Setter;

import static io.github.semlink.tensor.TensorflowFeatureUtils.int64Feature;
import static io.github.semlink.tensor.TensorflowFeatureUtils.int64Features;
import static io.github.semlink.tensor.TensorflowFeatureUtils.stringFeatures;

/**
 * Sequence example extractor that uses a {@link io.github.semlink.app.WordPieceTokenizer} to convert the input prior to normal
 * processing/extraction.
 *
 * @author jamesgung
 */
public class BertSrlExampleExtractor implements SequenceExampleExtractor {

    public static final String BERT_CLS = "[CLS]";
    public static final String BERT_SEP = "[SEP]";

    @Setter
    private String predicateIndexKey = "predicate_index";
    @Setter
    private String bertSplitIndex = "bert_split_idx";
    @Setter
    private String wordsKey = "word";
    @Setter
    private String bertIdsKey = "bert";
    @Setter
    private String markerKey = "marker";
    @Setter
    private String maskKey = "sequence_mask"; // mask use to ignore-subword inputs
    @Setter
    private String lengthKey = "len";
    @Setter
    private String sentenceIndexKey = "sentence_idx";
    @Setter
    private String goldKey = "gold";

    private WordPieceTokenizer wordPieceTokenizer;

    public BertSrlExampleExtractor(@NonNull WordPieceTokenizer tokenizer) {
        this.wordPieceTokenizer = tokenizer;
    }

    @Override
    public SequenceExample extractSequence(@NonNull HasFields sequence) {
        List<String> predicateIndices = sequence.field(predicateIndexKey);
        int predicateIndex = Integer.parseInt(predicateIndices.get(0));
        List<String> words = sequence.field(wordsKey);

        List<String> splitTokens = new ArrayList<>();
        List<Integer> maskValues = new ArrayList<>();
        int predicateSplitTokenIndex = -1; // index of predicate within wordpiece tokenization
        List<String> predicateSplitTokens = new ArrayList<>();

        // [CLS], word_1, word_2, ...
        splitTokens.add(BERT_CLS);
        maskValues.add(0);

        int tokenIndex = 0; // current token index
        for (String token : words) {
            List<String> subtokens = wordPieceTokenizer.tokenize(token);

            if (predicateIndex == tokenIndex) {
                predicateSplitTokenIndex = splitTokens.size();
                predicateSplitTokens = subtokens;
            }

            splitTokens.addAll(subtokens);
            maskValues.add(1);
            if (subtokens.size() > 0) {
                maskValues.addAll(Collections.nCopies(subtokens.size() - 1, 0));
            }

            ++tokenIndex;
        }

        // ..., word_n-1, word_n, [SEP]
        splitTokens.add(BERT_SEP);
        maskValues.add(0);

        final int splitIndex = splitTokens.size();

        // predicate_subtoken_1, predicate_subtoken2, ..., [SEP]
        splitTokens.addAll(predicateSplitTokens);
        maskValues.addAll(Collections.nCopies(predicateSplitTokens.size(), 0));

        splitTokens.add(BERT_SEP);
        maskValues.add(0);

        final int predIndex = predicateSplitTokenIndex;
        List<String> markers = IntStream.range(0, splitTokens.size())
            .mapToObj(i -> i == predIndex ? "1" : "0")
            .collect(Collectors.toList());

        FeatureLists.Builder featureLists = FeatureLists.newBuilder()
            // IDs for WordPiece tokens
            .putFeatureList(bertIdsKey, int64Features(wordPieceTokenizer.convertTokensToIds(splitTokens)))
            // mask used to ignore subtokens in prediction
            .putFeatureList(maskKey, int64Features(maskValues))
            // binary predicate marker embedding
            .putFeatureList(markerKey, stringFeatures(markers))
            // boiler plate
            .putFeatureList(goldKey, stringFeatures(Collections.nCopies(splitTokens.size(), "O")));

        Features.Builder features = Features.newBuilder()
            // index of first predicate subtoken within WordPiece tokens
            .putFeature(predicateIndexKey, int64Feature(predIndex))
            // index in subtokens of start of predicate sequence (second sequence, not original tokens)
            .putFeature(bertSplitIndex, int64Feature(splitIndex))
            // boiler plate
            .putFeature(lengthKey, int64Feature(splitTokens.size()))
            .putFeature(sentenceIndexKey, int64Feature(0));

        return SequenceExample.newBuilder()
            .setContext(features)
            .setFeatureLists(featureLists)
            .build();
    }

    @Override
    public Optional<Vocabulary> vocabulary(@NonNull String key) {
        return Optional.empty();
    }

}
