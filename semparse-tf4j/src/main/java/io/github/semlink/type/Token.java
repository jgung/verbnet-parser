package io.github.semlink.type;

import lombok.Getter;
import lombok.experimental.Accessors;

import static io.github.semlink.type.Fields.DefaultFields.TEXT;

/**
 * Default {@link IToken} implementation.
 *
 * @author jgung
 */
public class Token extends Fields implements IToken {

    @Getter
    @Accessors(fluent = true)
    private final int index;

    public Token(String text, int index) {
        this.index = index;
        add(TEXT, text);
    }

    @Override
    public String text() {
        return field(TEXT);
    }

    @Override
    public String toString() {
        return text();
    }
}
