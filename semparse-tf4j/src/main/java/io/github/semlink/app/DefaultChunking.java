package io.github.semlink.app;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Default {@link Chunking} implementation.
 *
 * @author jgung
 */
@AllArgsConstructor
public class DefaultChunking<T> implements Chunking<T> {

    @Getter
    @Accessors(fluent = true)
    private final List<Span<T>> spans;

    @Override
    public Span<T> span(int index) {
        for (Span<T> span : spans) {
            if (span.startIndex() <= index && span.endIndex() >= index) {
                return span;
            }
        }
        return null;
    }

    @Override
    public List<Span<T>> spans(@NonNull T label) {
        return spans.stream().filter(s -> s.label().equals(label)).collect(Collectors.toList());
    }

    @Override
    public <V> String toString(@NonNull List<V> tokens) {
        return spans.stream().map(s -> s.toString(tokens)).collect(Collectors.joining("\n"));
    }

}
