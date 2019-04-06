package io.github.clearwsd.parser;

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
import lombok.NonNull;

/**
 * Semantic role labeling system.
 *
 * @author jgung
 */
public class SemanticRoleLabeler<A> {

    private ShallowParser shallowParser;
    private Function<String, A> argMapper;
    private Function<NlpFocus<DepNode, DepTree>, ITokenSequence> inputAdapter = RoleLabelerUtils::focus2Sequence;

    public SemanticRoleLabeler(@NonNull ShallowParser shallowParser, @NonNull Function<String, A> argMapper) {
        this.shallowParser = shallowParser;
        this.argMapper = argMapper;
    }

    public <T> List<Proposition<T, A>> parse(@NonNull DepTree tree, @NonNull List<SensePrediction<T>> predicates) {

        List<NlpFocus<DepNode, DepTree>> foci = predicates.stream()
                .map(rel -> new DefaultNlpFocus<>(rel.index(), tree.get(rel.index()), tree))
                .collect(Collectors.toList());

        if (foci.isEmpty()) {
            return Collections.emptyList();
        }

        List<ITokenSequence> seqs = foci.stream().map(inputAdapter).collect(Collectors.toList());
        List<Chunking<A>> chunkings = shallowParser.shallowParseBatch(seqs).stream()
                .map(chunks -> ShallowParserUtils.mapChunks(chunks, argMapper))
                .collect(Collectors.toList());

        Iterator<SensePrediction<T>> senses = predicates.iterator();
        return chunkings.stream()
                .map(chunking -> new Proposition<>(senses.next(), chunking))
                .collect(Collectors.toList());
    }

}
