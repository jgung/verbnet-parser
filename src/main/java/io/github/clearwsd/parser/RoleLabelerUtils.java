package io.github.clearwsd.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.clearwsd.tfnlp.app.ShallowParser;
import io.github.clearwsd.tfnlp.app.TensorflowModel;
import io.github.clearwsd.tfnlp.type.Fields;
import io.github.clearwsd.tfnlp.type.HasFields;
import io.github.clearwsd.tfnlp.type.IToken;
import io.github.clearwsd.tfnlp.type.ITokenSequence;
import io.github.clearwsd.tfnlp.type.Token;
import io.github.clearwsd.tfnlp.type.TokenSequence;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import lombok.NonNull;

import static io.github.clearwsd.tfnlp.app.ShallowParserUtils.Tag.OUT;

/**
 * Semantic role labeling utilities.
 *
 * @author jgung
 */
public final class RoleLabelerUtils {

    private static final String PREDICATE_INDEX_KEY = "predicate_index";
    private static final String MARKER_KEY = "marker";
    private static final String WORD_KEY = "word";
    private static final String LABEL_KEY = "gold";

    public static ITokenSequence focus2Sequence(NlpFocus<DepNode, DepTree> tree) {
        List<IToken> tokens = new ArrayList<>();
        for (DepNode node : tree.tokens()) {
            Token token = new Token(node.feature(FeatureType.Text), node.index());
            tokens.add(token);
        }
        TokenSequence sequence = new TokenSequence(tokens);
        sequence.add(PREDICATE_INDEX_KEY, tree.focus().index());
        return sequence;
    }

    private static HasFields shallowSemParseFeatures(ITokenSequence tokens) {
        List<String> words = tokens.stream().map(IToken::text).collect(Collectors.toList());
        int predicateIndex = tokens.field(PREDICATE_INDEX_KEY);

        Fields seq = new Fields();
        seq.add(WORD_KEY, words);
        seq.add(LABEL_KEY, Collections.nCopies(words.size(), OUT.prefix()));
        seq.add(MARKER_KEY, IntStream.range(0, words.size())
                .mapToObj(i -> i == predicateIndex ? String.valueOf(1) : String.valueOf(0))
                .collect(Collectors.toList()));
        seq.add(PREDICATE_INDEX_KEY, Collections.singletonList(String.valueOf(predicateIndex)));
        return seq;
    }

    public static ShallowParser shallowSemanticParser(@NonNull String modelDir) {
        return new ShallowParser(TensorflowModel.fromDirectory(modelDir),
                RoleLabelerUtils::shallowSemParseFeatures);
    }

}
