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

package io.github.semlink.tfnlp;

import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.semlink.app.ShallowParserUtils;
import io.github.semlink.app.Span;
import io.github.semlink.app.TensorflowModel;
import io.github.semlink.extractor.SequenceExampleExtractor;
import io.github.semlink.extractor.config.ConfigSpec;
import io.github.semlink.extractor.config.Extractors;
import io.github.semlink.type.Fields;
import io.github.semlink.type.HasFields;
import io.github.semlink.type.IToken;
import io.github.semlink.type.Token;

/**
 * @author jamesgung
 */
public class ModelTest {

    private static final String EXPORT_DIR = "src/main/resources/models/propbank-srl/";

    private static SequenceExampleExtractor exampleExtractor() throws IOException {
        try (FileInputStream in = new FileInputStream(EXPORT_DIR + "config.json")) {
            ConfigSpec spec = ConfigSpec.fromInputStream(in);
            return Extractors.createExtractor(spec.features(), EXPORT_DIR + "vocab", true);
        }
    }

    private static HasFields getExample(List<String> words) {
        Fields seq = new Fields();
        seq.add("word", words);
        seq.add("predicate_index", 2);
        return seq;
    }

    @Test
    @Ignore
    public void build() {
        List<String> words = Arrays.asList("The cat sat on the mat .".split(" "));
        HasFields sequenceExample = getExample(words);

        try (TensorflowModel model = TensorflowModel.fromDirectory(EXPORT_DIR)) {
            List<String> labels = model.predict(sequenceExample);
            List<IToken> tokens = IntStream.range(0, words.size())
                    .mapToObj(i -> new Token(words.get(i), i))
                    .collect(Collectors.toList());
            List<Span<String>> spans = ShallowParserUtils.tags2Spans(labels);

            System.out.println(spans.stream().map(span -> span.label()
                    + "[" + span.get(tokens).stream()
                    .map(IToken::toString)
                    .collect(Collectors.joining(" ")) + "]"
            ).collect(Collectors.joining(" ")));
        }

    }

}


