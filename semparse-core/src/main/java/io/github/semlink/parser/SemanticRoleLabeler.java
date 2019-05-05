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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import lombok.NonNull;

/**
 * Semantic role labeler, which produces semantic role labels over an input {@link DepTree dependency tree} with respect to a list
 * of {@link SensePrediction predicates}.
 *
 * @param <A> argument type
 * @author jgung
 */
public interface SemanticRoleLabeler<A> {

    /**
     * Apply semantic role labeling to an input {@link DepTree dependency parse} with respect to a list of {@link SensePrediction
     * predicates}. A single {@link Proposition proposition} is created for each input predicate.
     *
     * @param tree       input dependency parse tree
     * @param predicates indices of predicates in the tree
     * @return a list of propositions corresponding to role labels for each input predicate
     */
    List<Proposition<DepNode, A>> parse(@NonNull DepTree tree, @NonNull List<Integer> predicates);

    /**
     * Apply semantic role labeling to an input {@link DepTree dependency parse} with respect to a single {@link SensePrediction
     * predicate}.
     *
     * @param tree input dependency parse tree
     * @return a proposition corresponding to a role labeling of the input tree with respect to the input predicate
     */
    default Proposition<DepNode, A> parse(@NonNull DepTree tree, int index) {
        return parse(tree, Collections.singletonList(index)).get(0);
    }

    static <R, A> Proposition<R, A> convert(Proposition<?, A> proposition, R rel) {
        return new Proposition<>(proposition.relIndex(), rel, proposition.arguments());
    }

    static <R, A> List<Proposition<R, A>> convert(List<? extends Proposition<?, A>> propositions, List<R> rels) {
        Iterator<R> relIterator = rels.iterator();
        return propositions.stream().map(prop -> convert(prop, relIterator.next())).collect(Collectors.toList());
    }

}
