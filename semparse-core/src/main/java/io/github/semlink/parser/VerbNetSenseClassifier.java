package io.github.semlink.parser;

import java.util.List;
import java.util.stream.Collectors;

import edu.mit.jverbnet.data.IVerbClass;
import io.github.clearwsd.DefaultSensePrediction;
import io.github.clearwsd.DefaultSensePredictor;
import io.github.clearwsd.ParsingSensePredictor;
import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.parser.Nlp4jDependencyParser;
import io.github.clearwsd.type.DepTree;
import io.github.semlink.verbnet.VerbNet;
import io.github.semlink.verbnet.VerbNetClass;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * VerbNet-specific {@link ParsingSensePredictor} implementation with {@link VerbNetClass} senses.
 *
 * @author jgung
 */
@AllArgsConstructor
public class VerbNetSenseClassifier implements ParsingSensePredictor<VerbNetClass> {

    private ParsingSensePredictor<IVerbClass> basePredictor;
    private VerbNet verbNet;

    @Override
    public List<SensePrediction<VerbNetClass>> predict(@NonNull DepTree depTree) {
        List<SensePrediction<IVerbClass>> senses = basePredictor.predict(depTree);
        return senses.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<SensePrediction<VerbNetClass>> predict(List<String> list) {
        List<SensePrediction<IVerbClass>> predict = basePredictor.predict(list);
        return predict.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public DepTree parse(@NonNull List<String> tokens) {
        return basePredictor.parse(tokens);
    }

    @Override
    public List<String> segment(@NonNull String document) {
        return basePredictor.segment(document);
    }

    @Override
    public List<String> tokenize(@NonNull String sentence) {
        return basePredictor.tokenize(sentence);
    }

    private SensePrediction<VerbNetClass> convert(@NonNull SensePrediction<IVerbClass> sense) {
        VerbNetClass result = sense.sense() == null ? null : verbNet.byId(sense.id()).orElse(null);
        return new DefaultSensePrediction<>(sense.index(), sense.originalText(), sense.id(), result);
    }

    /**
     * Initialize from a given {@link io.github.clearwsd.SensePredictor} model path and {@link VerbNet} lexicon.
     */
    public static VerbNetSenseClassifier fromModelPath(@NonNull String modelPath, @NonNull VerbNet verbNet) {
        DefaultSensePredictor<IVerbClass> predictor = DefaultSensePredictor.loadFromResource(modelPath,
                new Nlp4jDependencyParser());
        return new VerbNetSenseClassifier(predictor, verbNet);
    }

    /**
     * Initialize from a given {@link io.github.clearwsd.SensePredictor} model path.
     */
    public static VerbNetSenseClassifier fromModelPath(@NonNull String modelPath) {
        return fromModelPath(modelPath, new VerbNet());
    }

}
