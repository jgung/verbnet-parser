package io.github.semlink.clearwsd;

import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.type.DefaultDepNode;
import io.github.clearwsd.type.DefaultDepTree;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.parser.DependencyParser;
import io.github.semlink.type.Fields;
import io.github.semlink.type.HasFields;
import io.github.semlink.type.IToken;
import io.github.semlink.type.ITokenSequence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tensorflow-based {@link io.github.clearwsd.parser.NlpParser} implementation.
 *
 * @author jamesgung
 */
@Slf4j
@AllArgsConstructor
public class TfNlpParser implements NlpParser {

    private static final String WORD_KEY = "word";
    private static final String POS_KEY = "pos";
    private static final String DEP_KEY = "dep";
    private static final String HEAD_KEY = "head";

    private DependencyParser dependencyParser;
    private WnLemmatizer lemmatizer = new WnLemmatizer();
    private int batchSize = 16;

    public TfNlpParser(DependencyParser dependencyParser) {
        this.dependencyParser = dependencyParser;
    }

    @Override
    public DepTree parse(List<String> list) {
        return batchParse(Collections.singletonList(list)).get(0);
    }

    private List<DepTree> batchParse(List<List<String>> instances) {
        List<HasFields> inputs = instances.stream()
            .map(TfNlpParser::convert)
            .collect(Collectors.toList());

        List<ITokenSequence> results = dependencyParser.predictBatch(inputs);

        return results.stream()
            .map(this::convert)
            .collect(Collectors.toList());
    }

    public static Fields convert(List<String> tokens) {
        Fields seq = new Fields();
        seq.add(WORD_KEY, tokens);
        return seq;
    }

    private DepTree convert(ITokenSequence tokenSequence) {
        List<DepNode> tokens = new ArrayList<>();
        DepNode root = null;

        Map<Integer, Integer> headMap = new HashMap<>();
        for (IToken token : tokenSequence) {
            DefaultDepNode node = new DefaultDepNode(tokens.size());
            node.addFeature(FeatureType.Text, token.field(Fields.DefaultFields.TEXT));
            node.addFeature(FeatureType.Pos, token.field(POS_KEY));
            node.addFeature(FeatureType.Dep, token.field(DEP_KEY));
            node.addFeature(FeatureType.Lemma, lemmatizer.lemmatize(token.field(Fields.DefaultFields.TEXT), token.field(POS_KEY)));
            int headIndex = ((int) token.field(HEAD_KEY)) - 1;
            if (headIndex < 0) {
                root = node;
            } else {
                headMap.put(tokens.size(), headIndex);
            }

            tokens.add(node);
        }
        headMap.forEach((key, val) -> ((DefaultDepNode) tokens.get(key)).head(tokens.get(val)));

        return new DefaultDepTree(0, tokens, root);
    }

    public List<DepTree> parseBatch(List<List<String>> sentences) {
        List<DepTree> result = new ArrayList<>();

        List<List<String>> currentBatch = new ArrayList<>(batchSize);
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (List<String> tokens : sentences) {
            if (currentBatch.size() == batchSize) {
                result.addAll(batchParse(currentBatch));
                if (result.size() % (batchSize * 10) == 0) {
                    log.debug("{} trees parsed @ ~{}/sec", result.size(), result.size() / stopwatch.elapsed(TimeUnit.SECONDS));
                }
                currentBatch.clear();
            }
            currentBatch.add(tokens);
        }
        if (!currentBatch.isEmpty()) {
            result.addAll(batchParse(currentBatch));
        }
        return result;
    }

    @Override
    public List<String> segment(String passage) {
        return Arrays.asList(passage.split("\n"));
    }

    @Override
    public List<String> tokenize(String sentence) {
        return Arrays.asList(sentence.split(" "));
    }

}
