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

package io.github.semlink.parser;

import com.google.common.base.Stopwatch;
import io.github.semlink.app.SentencePieceTokenizer;
import io.github.semlink.parser.feat.BertDepExampleExtractor;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.example.SequenceExample;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import io.github.semlink.extractor.SequenceExampleExtractor;
import io.github.semlink.extractor.Vocabulary;
import io.github.semlink.tensor.TensorList;
import io.github.semlink.type.Fields;
import io.github.semlink.type.HasFields;
import lombok.NonNull;
import lombok.Setter;

import static io.github.semlink.tensor.Tensors.batchExamples;
import static io.github.semlink.tensor.Tensors.toFloatArrays;

/**
 * Tensorflow-based sentence classifier.
 *
 * @author jgung
 */
public class Classifier implements AutoCloseable {

    private static final String OP_NAME = "input_example_tensor";

    private SequenceExampleExtractor featureExtractor;
    private SavedModelBundle model;
    private Vocabulary vocabulary;

    @Setter
    private String labelsTensor = "gold/Softmax";

    public Classifier(@NonNull SequenceExampleExtractor featureExtractor,
                      @NonNull SavedModelBundle model,
                      @NonNull Vocabulary vocabulary) {
        this.featureExtractor = featureExtractor;
        this.model = model;
        this.vocabulary = vocabulary;
    }

    public List<Map<String, Float>> predictBatch(@NonNull List<HasFields> inputs) {

        List<SequenceExample> sequenceExamples = inputs.stream()
                .map(featureExtractor::extractSequence)
                .collect(Collectors.toList());

        try (Tensor<?> inputTensor = Tensor.create(batchExamples(sequenceExamples), String.class)) {
            Session.Runner runner = model.session().runner()
                    .feed(OP_NAME, inputTensor)
                    .fetch(labelsTensor);

            try (TensorList results = TensorList.of(runner.run())) {
                List<float[]> scores = toFloatArrays(results.get(0));

                List<Map<String, Float>> result = new ArrayList<>();
                for (float[] instance : scores) {
                    Map<String, Float> scoreMap = new HashMap<>();
                    for (int i = 0; i < instance.length; ++i) {
                        scoreMap.put(vocabulary.indexToFeat(i), instance[i]);
                    }
                    result.add(scoreMap);
                }
                return result;
            }
        }
    }


    public static Classifier fromDirectory(@NonNull String modelDir) {
        SequenceExampleExtractor extractor = new BertDepExampleExtractor(
                new SentencePieceTokenizer(Paths.get(modelDir, "model", "assets", "30k-clean.model").toString()))
            .maskSubtokens(false);
        SavedModelBundle model = SavedModelBundle.load(Paths.get(modelDir, "model").toString(), "serve");
        try (FileInputStream vocabStream = new FileInputStream(Paths.get(modelDir, "vocab", "gold").toString())) {
            return new Classifier(extractor, model, Vocabulary.read(vocabStream, "2"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        model.close();
    }

    public static void main(String[] args) {
        String path = args[0];

        try (Classifier model = Classifier.fromDirectory(path)) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print(">>> ");
                String line = scanner.nextLine();
                if (line.equals("QUIT")) {
                    break;
                }
                Fields seq = new Fields()
                        .add("word", Arrays.asList(line.split("\\s+")));
                Stopwatch started = Stopwatch.createStarted();
                Map<String, Float> result = model.predictBatch(Collections.singletonList(seq)).get(0);
                System.out.println("Elapsed time: " + started);
                System.out.println(result + "\n" + result.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElseThrow(IllegalArgumentException::new).getKey());
            }
        }
    }


}
