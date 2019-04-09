package io.github.clearwsd.parser;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.tfnlp.app.Chunking;
import io.github.clearwsd.tfnlp.app.ShallowParser;
import io.github.clearwsd.tfnlp.app.ShallowParserUtils;
import io.github.clearwsd.tfnlp.type.ITokenSequence;
import io.github.clearwsd.type.DefaultNlpFocus;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Default {@link SemanticRoleLabeler} implementation.
 *
 * @author jgung
 */
@AllArgsConstructor
public class DefaultSemanticRoleLabeler<A> implements SemanticRoleLabeler<A> {

    private ShallowParser shallowParser;
    private Function<String, A> argMapper;
    private Function<NlpFocus<DepNode, DepTree>, ITokenSequence> inputAdapter;

    /**
     * Initialize a semantic role labeler that maps output of a shallow parser to a given argument type using a provided mapping
     * function.
     *
     * @param shallowParser base shallow parser, which produces string labels irrespective of the argument type
     * @param argMapper     function mapping labels output by the shallow parser onto the correct argument type
     */
    public DefaultSemanticRoleLabeler(@NonNull ShallowParser shallowParser, @NonNull Function<String, A> argMapper) {
        this(shallowParser, argMapper, RoleLabelerUtils::focus2Sequence);
    }

    @Override
    public <T> List<Proposition<T, A>> parse(@NonNull DepTree tree, @NonNull List<SensePrediction<T>> predicates) {
        if (predicates.isEmpty()) {
            return Collections.emptyList();
        }

        // (1) map dependency parse tree to an input sequence of features given each predicate
        List<ITokenSequence> featsGivenPredicate = predicates.stream()
                .map(predicate -> new DefaultNlpFocus<>(predicate.index(), tree.get(predicate.index()), tree))
                .map(inputAdapter)
                .collect(Collectors.toList());

        // (2) apply shallow parser to feature inputs as a single batch
        List<Chunking<A>> chunkings = shallowParser.shallowParseBatch(featsGivenPredicate).stream()
                .map(chunks -> ShallowParserUtils.mapChunks(chunks, argMapper))
                .collect(Collectors.toList());
        Preconditions.checkState(chunkings.size() == predicates.size());

        // (3) map batched predictions to each input proposition
        Iterator<SensePrediction<T>> senses = predicates.iterator();
        return chunkings.stream()
                .map(chunking -> new Proposition<>(senses.next(), chunking))
                .collect(Collectors.toList());
    }

}
