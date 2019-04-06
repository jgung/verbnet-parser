package io.github.clearwsd.parser;

import java.util.List;
import java.util.stream.Collectors;

import edu.mit.jverbnet.data.IVerbClass;
import io.github.clearwsd.DefaultSensePrediction;
import io.github.clearwsd.DefaultSensePredictor;
import io.github.clearwsd.ParsingSensePredictor;
import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.verbnet.VerbNet;
import io.github.clearwsd.verbnet.VerbNetClass;
import lombok.NonNull;

/**
 * VerbNet classifier.
 *
 * @author jgung
 */
public class VerbNetSenseClassifier implements ParsingSensePredictor<VerbNetClass> {

    private ParsingSensePredictor<IVerbClass> basePredictor;
    private VerbNet verbNet;

    public VerbNetSenseClassifier(@NonNull ParsingSensePredictor<IVerbClass> basePredictor, @NonNull VerbNet verbNet) {
        this.verbNet = verbNet;
        this.basePredictor = basePredictor;
    }

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

    public static VerbNetSenseClassifier fromModelPath(@NonNull String modelPath) {
        DefaultSensePredictor<IVerbClass> predictor = DefaultSensePredictor.loadFromResource(modelPath,
                new Nlp4jDependencyParser());
        VerbNet verbNet = new VerbNet();
        return new VerbNetSenseClassifier(predictor, verbNet);
    }

}
