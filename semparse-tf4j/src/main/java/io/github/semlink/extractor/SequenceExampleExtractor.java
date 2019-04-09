package io.github.semlink.extractor;

import org.tensorflow.example.Feature;
import org.tensorflow.example.FeatureList;
import org.tensorflow.example.FeatureLists;
import org.tensorflow.example.Features;
import org.tensorflow.example.SequenceExample;

import java.util.List;

import io.github.semlink.type.HasFields;
import lombok.AllArgsConstructor;

/**
 * Aggregate feature extractor.
 *
 * @author jgung
 */
@AllArgsConstructor
public class SequenceExampleExtractor {

    private List<Extractor<FeatureList>> featureListExtractors;
    private List<Extractor<Feature>> featureExtractors;

    public SequenceExample extractSequence(HasFields sequence) {
        FeatureLists.Builder featureLists = FeatureLists.newBuilder();
        for (Extractor<FeatureList> featureListExtractor : featureListExtractors) {
            featureLists.putFeatureList(featureListExtractor.name(), featureListExtractor.extract(sequence));
        }
        Features.Builder features = Features.newBuilder();
        for (Extractor<Feature> featureExtractor : featureExtractors) {
            features.putFeature(featureExtractor.name(), featureExtractor.extract(sequence));
        }
        return SequenceExample.newBuilder()
                .setContext(features)
                .setFeatureLists(featureLists)
                .build();
    }

}
