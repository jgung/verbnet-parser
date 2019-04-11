package io.github.semlink.extractor;

import org.tensorflow.example.Feature;
import org.tensorflow.example.Int64List;

import java.util.List;

import io.github.semlink.type.HasFields;
import lombok.experimental.Accessors;

/**
 * Feature extractor for lengths of sequences.
 *
 * @author jgung
 */
@Accessors(fluent = true)
public class LengthExtractor extends KeyExtractor<Feature> {

    private static final String LENGTH_KEY = "len";

    public LengthExtractor(String key) {
        super(LENGTH_KEY, key);
    }

    @Override
    public Feature extract(HasFields seq) {
        Feature.Builder builder = Feature.newBuilder();
        int length = getValues(seq).size();
        builder.setInt64List(Int64List.newBuilder().addValue(length));
        return builder.build();
    }

    private List<String> getValues(HasFields seq) {
        return seq.field(key);
    }

}
