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

package io.github.semlink.verbnet.type;

import java.util.Optional;

import io.github.semlink.verbnet.syntax.VnLex;
import lombok.NonNull;

/**
 * Lexical element within a VerbNet syntactic frame.
 *
 * @author jgung
 */
public class LexicalElement extends FramePhrase {

    private VnLex lex;

    public LexicalElement(@NonNull VnLex lex) {
        super(VerbNetSyntaxType.LEX);
        this.lex = lex;
    }

    public LexType value() {
        return LexType.fromString(lex.value()).orElse(LexType.NONE);
    }

    public enum LexType {
        AND,
        APART,
        AS,
        AT,
        AWAY,
        BE,
        DOWN,
        IT,
        IT_BE,
        LIKE,
        OF,
        OUT,
        S,
        THERE,
        TO,
        TO_BE,
        TOGETHER,
        UP,
        NONE;

        public static Optional<LexType> fromString(@NonNull String string) {
            try {
                string = string.toUpperCase()
                        .replaceAll("[]+\\[']", " ")
                        .trim()
                        .replaceAll(" +", "_");

                return Optional.of(LexType.valueOf(string));
            } catch (Exception ignored) {
                return Optional.empty();
            }
        }
    }

    @Override
    public String toString() {
        return type() + "[" + value() + "]";
    }
}
