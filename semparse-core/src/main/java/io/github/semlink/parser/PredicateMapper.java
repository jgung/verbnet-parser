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

import java.util.Optional;

import io.github.clearwsd.type.DepNode;
import io.github.semlink.app.Span;
import lombok.NonNull;

/**
 * Predicate mapper, taking a token and mapping it to a new span/sense for further analysis. For example, mapping a verb to a
 * given light verb's nominal predicate.
 *
 * @author jgung
 */
public interface PredicateMapper<T> {

    /**
     * Map a given relation to a new span/sense for further analysis.
     *
     * @param rel predicate/relation
     * @return optionally mapped span
     */
    Optional<Span<T>> mapPredicate(@NonNull DepNode rel);

}
