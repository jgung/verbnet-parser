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

import io.github.clearwsd.verbnet.syntax.VnLex;
import io.github.clearwsd.verbnet.syntax.VnNounPhrase;
import io.github.clearwsd.verbnet.syntax.VnPrep;
import io.github.clearwsd.verbnet.syntax.VnSyntax;
import io.github.semlink.semlink.AlignPhrase;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Phrase within a VerbNet syntactic frame.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
public class FramePhrase extends AlignPhrase {

    private VerbNetSyntaxType type;

    public FramePhrase(@NonNull VerbNetSyntaxType type) {
        super(0);
        this.type = type;
    }

    public static FramePhrase of(@NonNull VnSyntax phrase) {
        if (phrase instanceof VnNounPhrase) {
            return new NounPhrase((VnNounPhrase) phrase);
        } else if (phrase instanceof VnPrep) {
            return new Preposition((VnPrep) phrase);
        } else if (phrase instanceof VnLex) {
            return new LexicalElement((VnLex) phrase);
        }
        VerbNetSyntaxType type = VerbNetSyntaxType.valueOf(phrase.type().name());
        return new FramePhrase(type);
    }

    @Override
    public String toString() {
        return type.name();
    }

}
