package io.github.semlink.verbnet.type;

import io.github.clearwsd.verbnet.syntax.VnLex;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;


@Getter
@Accessors(fluent = true)
public class LexicalElement extends FramePhrase {

    private LexType value;

    public LexicalElement(LexType type) {
        super(VerbNetSyntaxType.LEX);
        this.value = type;
    }

    public static LexicalElement of(@NonNull VnLex desc) {
        LexicalElement element = new LexicalElement(LexType.NONE);
        LexType.fromString(desc.value()).ifPresent(val -> element.value = val);
        return element;
    }

    public enum LexType {
        AND,
        APART,
        AS,
        AT,
        AWAY,
        BE,
        DOWN,
        IT,
        IT_BE,
        LIKE,
        OF,
        OUT,
        S,
        THERE,
        TO,
        TO_BE,
        TOGETHER,
        UP,
        NONE;

        public static Optional<LexType> fromString(@NonNull String string) {
            try {
                string = string.toUpperCase()
                    .replaceAll("[]+\\[']", " ")
                    .trim()
                    .replaceAll(" +", "_");

                return Optional.of(LexType.valueOf(string));
            } catch (Exception ignored) {
                return Optional.empty();
            }
        }
    }

}
