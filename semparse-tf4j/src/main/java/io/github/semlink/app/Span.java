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
