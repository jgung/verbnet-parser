package io.github.semlink.extractor;

import org.tensorflow.example.Feature;
import org.tensorflow.example.Int64List;

import java.util.List;

import io.github.semlink.type.HasFields;
import lombok.experimental.Accessors;

/**
 * Feature extractor for scalars.
 *
 * @author jgung
 */
@Accessors(fluent = true)
public class ScalarExtractor extends KeyExtractor<Feature> {

    public ScalarExtractor(String name, String key) {
        super(name, key);
    }

    @Override
    public Feature extract(HasFields seq) {
        Feature.Builder builder = Feature.newBuilder();
        int value = getValues(seq).stream().findFirst().map(Integer::valueOf).orElse(0);
        builder.setInt64List(Int64List.newBuilder().addValue(value));
        return builder.build();
    }

    private List<String> getValues(HasFields seq) {
        return seq.field(key);
    }

}
