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

package io.github.semlink.parser;

import java.util.List;
import java.util.function.Function;

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

    /**
     * Token index of the relation/predicate/main event/verb of this proposition.
     */
    @Getter
    private final int relIndex;
    /**
     * Relation/predicate/main event/verb of this proposition.
     */
    @Getter
    private final R predicate;
    /**
     * Labeled arguments of this proposition.
     */
    @Getter
    private final Chunking<A> arguments;

    /**
     * Span covering the relation.
     */
    public Span<A> relSpan() {
        return arguments.span(relIndex);
    }

    public String toString(@NonNull List<String> tokens, @NonNull Function<R, String> senseFormatter) {
        return senseFormatter.apply(predicate) + "\n" + arguments.toString(tokens);
    }

    public String toString(@NonNull List<String> tokens) {
        return toString(tokens, Object::toString);
    }

}
