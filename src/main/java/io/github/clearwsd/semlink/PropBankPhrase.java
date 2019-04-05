package io.github.clearwsd.semlink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.clearwsd.parser.Proposition;
import io.github.clearwsd.propbank.type.PropBankArg;
import io.github.clearwsd.tfnlp.app.Span;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

/**
 * Parsed PropBank phrase.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
public class PropBankPhrase extends AlignPhrase {

    private Span<PropBankArg> span;
    private DepTree parse;
    @Delegate
    private PropBankArg argument;

    public PropBankPhrase(int index, Span<PropBankArg> span, DepTree parse) {
        super(index);
        this.span = span;
        this.parse = parse;
        this.argument = span.label();
    }

    public List<DepNode> tokens() {
        return span.get(parse.tokens());
    }

    public DepNode start() {
        return parse.get(span.startIndex());
    }

    public DepNode end() {
        return parse.get(span.endIndex());
    }

    public static List<PropBankPhrase> fromProp(@NonNull Proposition<?, PropBankArg> proposition, @NonNull DepTree parse) {
        List<PropBankPhrase> result = new ArrayList<>();
        for (Span<PropBankArg> span : proposition.arguments().spans()) {
            result.add(new PropBankPhrase(result.size(), span, parse));
        }
        return result;
    }

    @Override
    public String toString() {
        return span.toString(parse);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

}
