package io.github.semlink.extractor.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * JSON-serializable extractor configuration.
 *
 * @author jgung
 */
@Getter
@Setter
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ExtractorSpec {

    @JsonProperty("seq_feat")
    private String seqFeat = "word";
    @JsonProperty("targets")
    private List<FeatureSpec> targets;
    @JsonProperty("inputs")
    private List<FeatureSpec> features;

}
