/*
 * Copyright 2019 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
