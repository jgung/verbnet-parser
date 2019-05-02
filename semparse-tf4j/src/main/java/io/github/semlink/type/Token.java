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

import lombok.Getter;
import lombok.experimental.Accessors;

import static io.github.semlink.type.Fields.DefaultFields.TEXT;

/**
 * Default {@link IToken} implementation.
 *
 * @author jgung
 */
public class Token extends Fields implements IToken {

    @Getter
    @Accessors(fluent = true)
    private final int index;

    public Token(String text, int index) {
        this.index = index;
        add(TEXT, text);
    }

    @Override
    public String text() {
        return field(TEXT);
    }

    @Override
    public String toString() {
        return text();
    }
}
