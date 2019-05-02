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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Extractor that returns values for a given key on inputs.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
public abstract class KeyExtractor<T> implements Extractor<T> {

    @Setter
    protected List<Function<String, String>> mappingFunctions = new ArrayList<>();

    private String name;
    protected String key;

    protected KeyExtractor(String name, String key) {
        this.name = name;
        this.key = key;
    }

    protected String map(String input) {
        for (Function<String, String> mappingFunction : mappingFunctions) {
            input = mappingFunction.apply(input);
        }
        return input;
    }

}
