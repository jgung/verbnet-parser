package io.github.semlink.extractor.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * NN configuration.
 *
 * @author jgung
 */
@Getter
@Setter
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ConfigSpec {

    private static ObjectMapper OM = new ObjectMapper();

    private ExtractorSpec features;

    public static ConfigSpec fromInputStream(InputStream inputStream) {
        try {
            return OM.readValue(inputStream, ConfigSpec.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read configuration file", e);
        }
    }

}
