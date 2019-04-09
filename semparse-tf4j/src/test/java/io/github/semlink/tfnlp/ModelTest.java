package io.github.semlink.tfnlp;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int64Value;

import org.junit.Ignore;
import org.junit.Test;
import org.tensorflow.example.SequenceExample;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;

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
import io.grpc.ManagedChannelBuilder;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

/**
 * @author jamesgung
 */
public class ModelTest {

    private static final String EXPORT_DIR = "src/main/resources/models/propbank/";

    private static SequenceExampleExtractor exampleExtractor() throws IOException {
        try (FileInputStream in = new FileInputStream(EXPORT_DIR + "config.json")) {
            ConfigSpec spec = ConfigSpec.fromInputStream(in);
            return Extractors.createExtractor(spec.features(), EXPORT_DIR + "vocab", true);
        }
    }

    private static HasFields getExample(List<String> words, int predicateIndex) {
        Fields seq = new Fields();
        seq.add("word", words);
        seq.add("gold", Collections.nCopies(words.size(), "O"));

        seq.add("marker", IntStream.range(0, words.size())
                .mapToObj(i -> i == predicateIndex ? "1" : "0")
                .collect(Collectors.toList()));
        seq.add("predicate_index", Collections.singletonList("" + predicateIndex));
        return seq;
    }

    @Test
    @Ignore
    public void build() {
        List<String> words = Arrays.asList("The cat sat on the mat .".split(" "));
        HasFields sequenceExample = getExample(words, 2);

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

    @Test
    @Ignore
    public void testServing() throws IOException {
        List<String> words = Arrays.asList("John Smith went to the store".split(" "));
        SequenceExampleExtractor exampleExtractor = exampleExtractor();
        HasFields fields = getExample(words, 3);
        SequenceExample sequenceExample = exampleExtractor.extractSequence(fields);
        // tensorflow serving
        TensorProto proto = TensorProto.newBuilder()
                .addStringVal(sequenceExample.toByteString())
                .setTensorShape(TensorShapeProto.newBuilder())
                .setDtype(DataType.DT_STRING)
                .build();
        Predict.PredictRequest req = Predict.PredictRequest.newBuilder()
                .setModelSpec(Model.ModelSpec.newBuilder()
                        .setName("best")
                        .setSignatureName("serving_default")
                        .setVersion(Int64Value.newBuilder().setValue(1536178255)))
                .putAllInputs(ImmutableMap.of("examples", proto))
                .build();


        PredictionServiceGrpc.PredictionServiceBlockingStub stub = PredictionServiceGrpc.newBlockingStub(
                ManagedChannelBuilder.forAddress("127.0.0.1", 8500).usePlaintext().build());

        Predict.PredictResponse predict = stub.predict(req);
        System.out.println(predict.getOutputsOrThrow("output").getStringValList().stream()
                .map(ByteString::toStringUtf8)
                .collect(Collectors.joining(" ")));
    }

}


