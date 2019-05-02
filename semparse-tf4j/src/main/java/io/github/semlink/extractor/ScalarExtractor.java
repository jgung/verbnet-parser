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
