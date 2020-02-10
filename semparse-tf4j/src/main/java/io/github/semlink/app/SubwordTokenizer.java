package io.github.semlink.app;

import java.util.List;
import lombok.NonNull;

/**
 * String-level tokenizer.
 *
 * @author jamesgung
 */
public interface SubwordTokenizer {

    List<Integer> convertTokensToIds(@NonNull List<String> tokens);

    List<String> tokenize(@NonNull String text);

}
