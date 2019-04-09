package io.github.semlink.parser;

import java.util.List;
import java.util.function.Function;

import io.github.clearwsd.SensePrediction;
import io.github.semlink.app.Chunking;
import io.github.semlink.app.Span;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Semantic role labeling proposition, a predicate with associated arguments.
 *
 * @param <R> relation type
 * @param <A> argument type
 * @author jgung
 */
@AllArgsConstructor
@Accessors(fluent = true)
public class Proposition<R, A> {

    @Getter
    private final SensePrediction<R> predicate;
    @Getter
    private final Chunking<A> arguments;

    public Span<A> relSpan() {
        return arguments.span(predicate.index());
    }

    public String toString(@NonNull List<String> tokens, @NonNull Function<SensePrediction<R>, String> senseFormatter) {
        return senseFormatter.apply(predicate) + "\n" + arguments.toString(tokens);
    }

    public String toString(@NonNull List<String> tokens) {
        return toString(tokens, s -> s.originalText() + " " + s.id());
    }

}
