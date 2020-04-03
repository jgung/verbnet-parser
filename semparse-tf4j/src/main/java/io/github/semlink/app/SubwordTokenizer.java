package io.github.semlink.app;

import java.util.List;
import lombok.NonNull;

/**
 * String-level tokenizer.
 *
 * @author jamesgung
 */
public interface SubwordTokenizer {

    /**
     * Convert a list of string tokens to IDs.
     */
    List<Integer> convertTokensToIds(@NonNull List<String> tokens);

    /**
     * Tokenize a string.
     */
    List<String> tokenize(@NonNull String text);

}
