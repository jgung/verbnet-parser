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

package io.github.semlink.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    public static String capitalized(@NonNull Enum<?> value) {
        return capitalized(value.name());
    }

    public static String capitalized(@NonNull String name) {
        return Arrays.stream(name.split("_"))
                .map(s -> s.length() == 1 ? s.toUpperCase() : s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static String capitalize(@NonNull String string) {
        return string.length() == 1 ? string.toUpperCase()
                : string.substring(0, 1).toUpperCase() + string.substring(1);
    }

}
