package io.github.semlink.parser;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepTree;
import java.util.Collections;
import java.util.List;
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
     * @param tree input dependency parse tree
     * @param predicates predicates/sense predictions indexed to tokens in the tree
     * @param <T> type of sense for predicate
     * @return a list of propositions corresponding to role labels for each input predicate
     */
    <T> List<Proposition<T, A>> parse(@NonNull DepTree tree, @NonNull List<SensePrediction<T>> predicates);

    /**
     * Apply semantic role labeling to an input {@link DepTree dependency parse} with respect to a single {@link SensePrediction
     * predicate}.
     *
     * @param tree input dependency parse tree
     * @param predicate predicate/sense prediction indexed to a token in the tree
     * @param <T> type of sense for predicate
     * @return a proposition corresponding to a role labeling of the input tree with respect to the input predicate
     */
    default <T> Proposition<T, A> parse(@NonNull DepTree tree, @NonNull SensePrediction<T> predicate) {
        return parse(tree, Collections.singletonList(predicate)).get(0);
    }

}
