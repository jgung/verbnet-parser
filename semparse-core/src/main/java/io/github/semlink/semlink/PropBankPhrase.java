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

package io.github.semlink.semlink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.semlink.app.Span;
import io.github.semlink.parser.Proposition;
import io.github.semlink.propbank.type.PropBankArg;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

/**
 * Parsed PropBank phrase.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
public class PropBankPhrase extends AlignPhrase {

    private Span<PropBankArg> span;
    private DepTree parse;
    @Delegate
    private PropBankArg argument;

    public PropBankPhrase(int index, Span<PropBankArg> span, DepTree parse) {
        super(index);
        this.span = span;
        this.parse = parse;
        this.argument = span.label();
    }

    public List<DepNode> tokens() {
        return span.get(parse.tokens());
    }

    public DepNode start() {
        return parse.get(span.startIndex());
    }

    public DepNode end() {
        return parse.get(span.endIndex());
    }

    public static List<PropBankPhrase> fromProp(@NonNull Proposition<?, PropBankArg> proposition, @NonNull DepTree parse) {
        List<PropBankPhrase> result = new ArrayList<>();
        for (Span<PropBankArg> span : proposition.arguments().spans()) {
            result.add(new PropBankPhrase(result.size(), span, parse));
        }
        return result;
    }

    @Override
    public String toString() {
        return span.toString(parse);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

}
