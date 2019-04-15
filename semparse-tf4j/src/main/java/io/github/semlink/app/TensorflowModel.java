package io.github.semlink.app;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.example.SequenceExample;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.semlink.extractor.SequenceExampleExtractor;
import io.github.semlink.extractor.config.ConfigSpec;
import io.github.semlink.extractor.config.Extractors;
import io.github.semlink.tensor.TensorList;
import io.github.semlink.type.HasFields;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import static io.github.semlink.tensor.Tensors.batchExamples;
import static io.github.semlink.tensor.Tensors.toStringLists;

/**
 * Tensorflow sequence prediction model.
 *
 * @author jgung
 */
@AllArgsConstructor
public class TensorflowModel implements AutoCloseable, SequencePredictor<HasFields> {

    private static final String OP_NAME = "input_example_tensor";
    private static final String FETCH_NAME = "gold/labels";

    private SequenceExampleExtractor featureExtractor;
    private SavedModelBundle model;

    private String inputName = OP_NAME;
    private String fetchName = FETCH_NAME;

    public TensorflowModel(@NonNull SequenceExampleExtractor featureExtractor, @NonNull SavedModelBundle model) {
        this(featureExtractor, model, OP_NAME, FETCH_NAME);
    }

    @Override
    public List<String> predict(@NonNull HasFields input) {
        return predictBatch(Collections.singletonList(input)).get(0);
    }

    @Override
    public List<List<String>> predictBatch(@NonNull List<HasFields> inputs) {

        List<SequenceExample> sequenceExamples = inputs.stream()
                .map(featureExtractor::extractSequence)
                .collect(Collectors.toList());

        try (Tensor<?> inputTensor = Tensor.create(batchExamples(sequenceExamples), String.class)) {
            try (TensorList results = TensorList.of(model.session().runner()
                    .feed(inputName, inputTensor)
                    .fetch(fetchName)
                    .run())) {
                return toStringLists(results.get(0));
            }
        }
    }

    @Override
    public void close() {
        model.close();
    }

    public static TensorflowModel fromDirectory(@NonNull String modelDir) {
        try (FileInputStream in = new FileInputStream(Paths.get(modelDir, "config.json").toString())) {
            ConfigSpec spec = ConfigSpec.fromInputStream(in);
            SequenceExampleExtractor extractor = Extractors.createExtractor(spec.features(),
                    Paths.get(modelDir, "vocab").toString(), true);

            SavedModelBundle model = SavedModelBundle.load(Paths.get(modelDir, "model").toString(), "serve");
            return new TensorflowModel(extractor, model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
