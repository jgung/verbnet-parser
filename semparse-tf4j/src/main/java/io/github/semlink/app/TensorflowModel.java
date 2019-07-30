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

package io.github.semlink.app;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.example.SequenceExample;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.semlink.extractor.BertSrlExampleExtractor;
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
    private static final String IGNORE_LABEL = "X";

    private SequenceExampleExtractor featureExtractor;
    private SavedModelBundle model;

    private String inputName;
    private String fetchName;

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
            Session.Runner runner = model.session().runner()
                    .feed(inputName, inputTensor)
                    .fetch(fetchName);
            TensorList results;

            // TODO: figure out why Session is not thread safe, fix, unsynchronize
            synchronized (this) {
                results = TensorList.of(runner.run());
            }

            List<List<String>> result = toStringLists(results.get(0)).stream()
                    .map(labels -> labels.stream().filter(l -> !l.equals(IGNORE_LABEL)).collect(Collectors.toList()))
                    .collect(Collectors.toList());

            results.close();
            return result;
        }
    }

    @Override
    public void close() {
        model.close();
    }

    public static TensorflowModel fromDirectory(@NonNull String modelDir, @NonNull SequenceExampleExtractor featureExtractor) {
        SavedModelBundle model = SavedModelBundle.load(Paths.get(modelDir, "model").toString(), "serve");
        return new TensorflowModel(featureExtractor, model);
    }

    public static TensorflowModel fromDirectory(@NonNull String modelDir) {
        try (FileInputStream in = new FileInputStream(Paths.get(modelDir, "config.json").toString())) {
            ConfigSpec spec = ConfigSpec.fromInputStream(in);
            SequenceExampleExtractor extractor = Extractors.createExtractor(spec.features(),
                    Paths.get(modelDir, "vocab").toString(), false);

            return fromDirectory(modelDir, extractor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TensorflowModel bertFromDirectory(@NonNull String modelDir) {
        return fromDirectory(modelDir, new BertSrlExampleExtractor(
                new WordPieceTokenizer(Paths.get(modelDir, "model", "assets", "vocab.txt").toString())));
    }

}
