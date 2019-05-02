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

package io.github.semlink.type;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

/**
 * Default {@link HasFields} implementation.
 *
 * @author jgung
 */
public class Fields implements HasFields {

    private Map<String, Object> fieldMap = new HashMap<>();

    public <T> Fields add(String key, T value) {
        fieldMap.put(key, value);
        return this;
    }

    public <T> Fields add(Enum key, T value) {
        fieldMap.put(key.name(), value);
        return this;
    }

    @Override
    public <T> T field(String key) {
        //noinspection unchecked
        return (T) fieldMap.get(key);
    }

    @Override
    public <T> T field(@NonNull Enum key) {
        //noinspection unchecked
        return (T) fieldMap.get(key.name());
    }

    @Override
    public boolean hasFields(@NonNull EnumSet<?> keys) {
        return keys.stream().allMatch(key -> fieldMap.containsKey(key.name()));
    }

    @Override
    public boolean hasFields(@NonNull String... keys) {
        for (String key : keys) {
            if (!fieldMap.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public enum DefaultFields {
        TEXT,
        LABEL
    }

}
