package io.github.semlink.extractor;

import org.tensorflow.example.Feature;
import org.tensorflow.example.Int64List;

import io.github.semlink.type.HasFields;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Dummy feature extractor returning a constant value.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ConstantFeatureExtractor implements Extractor<Feature> {

    private String name;
    private int value;

    @Override
    public Feature extract(HasFields sequence) {
        Feature.Builder builder = Feature.newBuilder();
        builder.setInt64List(Int64List.newBuilder().addValue(value));
        return builder.build();
    }

}
