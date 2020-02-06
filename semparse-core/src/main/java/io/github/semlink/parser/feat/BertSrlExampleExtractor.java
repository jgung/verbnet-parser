package io.github.semlink.parser.feat;

import static io.github.semlink.app.TensorflowModel.fromDirectory;
import static io.github.semlink.tensor.TensorflowFeatureUtils.int64Feature;
import static io.github.semlink.tensor.TensorflowFeatureUtils.int64Features;

import com.google.common.base.Preconditions;
import io.github.semlink.app.TensorflowModel;
import io.github.semlink.app.WordPieceTokenizer;
import io.github.semlink.type.HasFields;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.Setter;
import org.tensorflow.example.FeatureLists;
import org.tensorflow.example.Features;
import org.tensorflow.example.SequenceExample;

/**
 * Sequence example extractor that uses a {@link io.github.semlink.app.WordPieceTokenizer} to convert the input prior to normal
 * processing/extraction.
 *
 * @author jamesgung
 */
public class BertSrlExampleExtractor extends BertExampleExtractor {

    public static final int SEGMENT_A = 0;
    public static final int SEGMENT_B = 1;

    /**
     * Indicates the original token index of the predicate.
     */
    @Setter
    private String predicateIndexKey = "predicate_index";
    /**
     * Placeholder feature name for BERT segment IDs.
     */
    @Setter
    private String segmentIdsKey = "bert_seg_ids";


    public BertSrlExampleExtractor(@NonNull WordPieceTokenizer wordPieceTokenizer) {
        super(wordPieceTokenizer);
    }

    @Override
    public SequenceExample extractSequence(@NonNull HasFields sequence) {
        final int predicateIndex = sequence.field(predicateIndexKey);
        final List<String> words = sequence.field(wordsKey);

        final List<String> splitTokens = new ArrayList<>();
        final List<Integer> maskValues = new ArrayList<>();
        final List<Integer> segmentIds = new ArrayList<>();

        List<String> predicateSplitTokens = new ArrayList<>();

        // [CLS], word_1, word_2, ...
        splitTokens.add(BERT_CLS);
        maskValues.add(0);
        segmentIds.add(SEGMENT_A);

        int tokenIndex = 0; // current token index
        for (String token : words) {
            List<String> subtokens = wordPieceTokenizer.tokenize(token);

            boolean isPredicate = predicateIndex == tokenIndex;
            if (isPredicate) {
                predicateSplitTokens = subtokens;
            }

            splitTokens.addAll(subtokens);
            maskValues.add(1);
            // this model indicates the focus predicate by setting the segment ID to B
            segmentIds.add(isPredicate ? SEGMENT_B : SEGMENT_A);

            if (subtokens.size() > 1) {
                maskValues.addAll(Collections.nCopies(subtokens.size() - 1, 0));
                segmentIds.addAll(Collections.nCopies(subtokens.size() - 1, SEGMENT_A));
            }

            ++tokenIndex;
        }

        // ..., word_n-1, word_n, [SEP]
        splitTokens.add(BERT_SEP);
        maskValues.add(0);
        segmentIds.add(0);

        // predicate_subtoken_1, predicate_subtoken2, ..., [SEP]
        splitTokens.addAll(predicateSplitTokens);
        maskValues.addAll(Collections.nCopies(predicateSplitTokens.size(), 0));
        segmentIds.addAll(Collections.nCopies(predicateSplitTokens.size(), SEGMENT_B));

        splitTokens.add(BERT_SEP);
        maskValues.add(0);
        segmentIds.add(SEGMENT_B);

        Preconditions.checkState(splitTokens.size() == maskValues.size()
                        && segmentIds.size() == maskValues.size(),
                "Number of segment IDs, wordpiece IDs, and mask values do not match: %s vs. %s vs %s",
                segmentIds.size(), splitTokens.size(), maskValues.size());

        FeatureLists.Builder featureLists = FeatureLists.newBuilder()
                // IDs for WordPiece tokens
                .putFeatureList(bertIdsKey, int64Features(wordPieceTokenizer.convertTokensToIds(splitTokens)))
                // mask used to ignore subtokens in prediction
                .putFeatureList(maskKey, int64Features(maskValues))
                // segment ids for segment embeddings passed as inputs to BERT
                .putFeatureList(segmentIdsKey, int64Features(segmentIds));

        Features.Builder features = Features.newBuilder()
                // index of first predicate subtoken within WordPiece tokens
                .putFeature(predicateIndexKey, int64Feature(predicateIndex))
                // length of wordpiece sequence input to BERT
                .putFeature(bertLengthKey, int64Feature(splitTokens.size()))
                // boiler plate
                .putFeature(lengthKey, int64Feature(words.size()))
                .putFeature(sentenceIndexKey, int64Feature(0));

        return SequenceExample.newBuilder()
                .setContext(features)
                .setFeatureLists(featureLists)
                .build();
    }

    public static TensorflowModel bertFromDirectory(@NonNull String modelDir) {
        return fromDirectory(modelDir, new BertSrlExampleExtractor(
                new WordPieceTokenizer(Paths.get(modelDir, "model", "assets", "vocab.txt").toString())));
    }

}
