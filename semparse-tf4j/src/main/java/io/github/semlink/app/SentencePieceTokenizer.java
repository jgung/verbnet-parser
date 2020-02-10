package io.github.semlink.app;

import com.github.google.sentencepiece.SentencePieceProcessor;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * SentencePiece tokenizer to produce inputs to models such as ALBERT.
 *
 * @author jamesgung
 * @see <a href="https://github.com/google/sentencepiece">https://github.com/google/sentencepiece</a>
 * @see <a href="https://github.com/levyfan/sentencepiece-jni">https://github.com/levyfan/sentencepiece-jni</a>
 */
@Slf4j
@Accessors(fluent = true)
public class SentencePieceTokenizer implements SubwordTokenizer, AutoCloseable {

    private SentencePieceProcessor sp;
    @Setter
    private boolean lowercase = true;

    /**
     * Read a SentencePiece model from a given path.
     *
     * @param modelPath model path
     * @return SentencePiece model
     */
    public static SentencePieceProcessor readModel(@NonNull String modelPath) {
        SentencePieceProcessor sp = new SentencePieceProcessor();
        sp.load(modelPath);
        return sp;
    }

    public SentencePieceTokenizer(@NonNull String path) {
        this.sp = readModel(path);
    }

    @Override
    public List<Integer> convertTokensToIds(@NonNull List<String> tokens) {
        return tokens.stream().map(s -> sp.pieceToId(s)).collect(Collectors.toList());
    }

    /**
     * Tokenize input text into a list of word pieces based on the vocabulary.
     *
     * @param text input text, a single token or whitespace separated sentence
     * @return list of word pieces
     */
    @Override
    public List<String> tokenize(@NonNull String text) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");
        if (lowercase) {
            text = text.toLowerCase();
        }
        return sp.encodeAsPieces(text);
    }

    @Override
    public void close() {
        if (null != sp) {
            sp.close();
        }
    }
}
