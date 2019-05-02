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

import com.google.protobuf.ByteString;

import org.tensorflow.example.BytesList;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FeatureList;

import java.util.List;

import io.github.semlink.type.HasFields;
import lombok.experimental.Accessors;

/**
 * Feature extractor.
 *
 * @author jgung
 */
@Accessors(fluent = true)
public class TextExtractor extends KeyExtractor<FeatureList> {

    public TextExtractor(String name, String key) {
        super(name, key);
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

    private List<String> getValues(HasFields seq) {
        return seq.field(key);
    }

}
