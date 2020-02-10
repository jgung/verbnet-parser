package io.github.semlink.parser.feat;

import io.github.semlink.app.SubwordTokenizer;
import io.github.semlink.extractor.SequenceExampleExtractor;
import io.github.semlink.extractor.Vocabulary;
import java.util.Optional;
import lombok.NonNull;
import lombok.Setter;

/**
 * Sequence example extractor for BERT-based models.
 *
 * @author jgung
 */
public abstract class BertExampleExtractor implements SequenceExampleExtractor {

    public static final String BERT_CLS = "[CLS]";
    public static final String BERT_SEP = "[SEP]";

    protected SubwordTokenizer wordPieceTokenizer;

    /**
     * Word inputs key from feature extraction.
     */
    @Setter
    protected String wordsKey = "word";
    /**
     * Placeholder feature name for BERT wordpiece indices.
     */
    @Setter
    protected String bertIdsKey = "bert";
    /**
     * Placeholder feature name for BERT mask over wordpiece tokens. Used to ignore subword inputs.
     */
    @Setter
    protected String maskKey = "sequence_mask";
    /**
     * Placeholder feature name for number of original tokens.
     */
    @Setter
    protected String lengthKey = "len";
    /**
     * Placeholder for total length of the wordpiece sequence input to BERT.
     */
    @Setter
    protected String bertLengthKey = "bert_len";
    /**
     * Placeholder for number used for tracking instance numbers during evaluation.
     */
    @Setter
    protected String sentenceIndexKey = "sentence_idx";

    public BertExampleExtractor(@NonNull SubwordTokenizer wordPieceTokenizer) {
        this.wordPieceTokenizer = wordPieceTokenizer;
    }

    @Override
    public Optional<Vocabulary> vocabulary(@NonNull String key) {
        return Optional.empty();
    }

}
