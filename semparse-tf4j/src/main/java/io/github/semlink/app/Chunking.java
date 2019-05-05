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

import lombok.NonNull;

/**
 * Chunking consisting of labeled spans. Takes any sequence and produces a corresponding list of phrases.
 *
 * @param <T> chunk type
 * @author jgung
 */
public interface Chunking<T> {

    /**
     * Returns the span covering tokens at a given index.
     */
    Span<T> span(int index);

    /**
     * Return all spans in this chunking.
     */
    List<Span<T>> spans();

    /**
     * Return a list of spans matching a given label.
     */
    List<Span<T>> spans(@NonNull T label);

    <V> String toString(@NonNull List<V> tokens);

}