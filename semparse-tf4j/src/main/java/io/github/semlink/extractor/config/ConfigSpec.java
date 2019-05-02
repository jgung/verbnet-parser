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
