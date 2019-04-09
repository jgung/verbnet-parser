package io.github.semlink.app;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Labeled span.
 *
 * @param <T> span type
 * @author jgung
 */
@Data
@Accessors(fluent = true)
public class Span<T> {

    public static <A> Span<A> convert(@NonNull Span<?> span, @NonNull A label) {
        return new Span<>(label, span.startIndex, span.endIndex);
    }

    private T label;
    private int startIndex;
    private int endIndex;

    public Span(@NonNull T label, int startIndex, int endIndex) {
        this.label = label;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public <V> List<V> get(@NonNull List<V> tokens) {
        return tokens.subList(startIndex, endIndex + 1);
    }

    public <V> String toString(@NonNull List<V> tokens) {
        return label + "[" + get(tokens).stream().map(Object::toString).collect(Collectors.joining(" ")) + "]";
    }

    @Override
    public String toString() {
        return label.toString() + "(" + startIndex + ", " + endIndex + ")";
    }

}
