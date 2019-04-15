package io.github.semlink.app;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.semlink.type.HasFields;
import io.github.semlink.type.IToken;
import io.github.semlink.type.ITokenSequence;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import static io.github.semlink.app.ShallowParserUtils.tags2Spans;

/**
 * Default {@link IShallowParser} implementation.
 *
 * @author jgung
 */
@AllArgsConstructor
public class ShallowParser implements IShallowParser<IToken, ITokenSequence> {

    private TensorflowModel predictor;
    private Function<ITokenSequence, HasFields> featureExtractor;

    @Override
    public Chunking<String> shallowParse(@NonNull ITokenSequence sequence) {
        return shallowParseBatch(Collections.singletonList(sequence)).get(0);
    }

    @Override
    public List<Chunking<String>> shallowParseBatch(@NonNull List<ITokenSequence> sequence) {
        List<HasFields> features = sequence.stream()
                .map(featureExtractor)
                .collect(Collectors.toList());

        List<List<String>> labels = predictor.predictBatch(features);
        Preconditions.checkState(labels.size() == sequence.size(),
                "Predictor produced an unexpected number of batches: %d vs. %d", labels.size(), sequence.size());

        Iterator<ITokenSequence> iterator = sequence.iterator();
        List<Chunking<String>> phrases = new ArrayList<>();
        for (List<String> labeling : labels) {
            int tokens = iterator.next().size();
            Preconditions.checkState(labeling.size() == tokens,
                    "Predictor produced an unexpected number of labels: %d vs. %d", tokens, labeling.size());
            phrases.add(new DefaultChunking<>(tags2Spans(labeling)));
        }

        return phrases;
    }

    @Override
    public void close() {
        predictor.close();
    }

}
