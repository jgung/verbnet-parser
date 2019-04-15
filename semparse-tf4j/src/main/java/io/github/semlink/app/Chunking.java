package io.github.semlink.app;

import java.util.List;

import lombok.NonNull;

/**
 * Chunking consisting of labeled spans. Takes any sequence and produces a corresponding list of phrases.
 *
 * @param <T> chunk type
 * @author jgung
 */
public interface Chunking<T> {

    Span<T> span(int index);

    List<Span<T>> spans();

    List<Span<T>> spans(@NonNull T label);

    <V> String toString(@NonNull List<V> tokens);

}