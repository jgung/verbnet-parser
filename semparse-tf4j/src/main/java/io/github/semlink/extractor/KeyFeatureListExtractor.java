package io.github.semlink.extractor;


import com.google.protobuf.ByteString;

import org.tensorflow.example.BytesList;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FeatureList;

import io.github.semlink.extractor.config.FeatureSpec;
import io.github.semlink.type.HasFields;
import lombok.experimental.Accessors;

/**
 * Feature extractor.
 *
 * @author jgung
 */
@Accessors(fluent = true)
public class KeyFeatureListExtractor extends BaseFeatureExtractor<FeatureList> {

    public KeyFeatureListExtractor(FeatureSpec featureSpec, Vocabulary vocabulary) {
        super(featureSpec.name(), featureSpec.key(), vocabulary);
    }

    @Override
    public FeatureList extract(HasFields seq) {
        FeatureList.Builder builder = FeatureList.newBuilder();
        getValues(seq).stream()
                .map(this::map)
                .map(text -> Feature.newBuilder().setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8(text))))
                .forEach(builder::addFeature);
        return builder.build();
    }

}
