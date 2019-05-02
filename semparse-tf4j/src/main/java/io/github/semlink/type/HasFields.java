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

import lombok.NonNull;

/**
 * Base unit used for NLP processing.
 *
 * @author jgung
 */
public interface HasFields {

    /**
     * Return the value for a given field on this token.
     *
     * @param key field key
     * @param <T> field type
     * @return field value
     */
    <T> T field(@NonNull String key);


    /**
     * Return the value for a given field on this token.
     *
     * @param key field key
     * @param <T> field type
     * @return field value
     */
    <T> T field(@NonNull Enum key);

    boolean hasFields(@NonNull EnumSet<?> keys);

    boolean hasFields(@NonNull String... keys);

}
