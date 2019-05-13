package io.github.semlink.tensor;

import com.google.protobuf.ByteString;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.tensorflow.example.BytesList;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FeatureList;
import org.tensorflow.example.Int64List;

/**
 * Tensorflow Feature proto utils.
 *
 * @author jamesgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TensorflowFeatureUtils {

    public static Feature int64Feature(@NonNull Integer feature) {
        return Feature.newBuilder()
            .setInt64List(Int64List.newBuilder().addValue(feature))
            .build();
    }

    public static FeatureList int64Features(@NonNull List<Integer> features) {
        FeatureList.Builder builder = FeatureList.newBuilder();
        features.stream()
            .map(val -> Feature.newBuilder().setInt64List(org.tensorflow.example.Int64List.newBuilder()
                .addValue((long) val)).build())
            .forEach(builder::addFeature);
        return builder.build();
    }

    public static FeatureList stringFeatures(@NonNull List<String> features) {
        FeatureList.Builder builder = FeatureList.newBuilder();
        features.stream()
            .map(text -> Feature.newBuilder().setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8(text))))
            .forEach(builder::addFeature);
        return builder.build();
    }

}
