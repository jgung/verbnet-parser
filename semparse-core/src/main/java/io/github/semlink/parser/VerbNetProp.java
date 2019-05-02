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

package io.github.semlink.parser;

import java.util.ArrayList;
import java.util.List;

import io.github.clearwsd.verbnet.VnClass;
import io.github.semlink.semlink.SemlinkRole;
import io.github.semlink.verbnet.semantics.SemanticPredicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * VerbNet proposition.
 *
 * @author jgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class VerbNetProp {

    private int tokenIndex;
    private List<String> tokens;
    private List<SemanticPredicate> predicates = new ArrayList<>();
    private Proposition<VnClass, SemlinkRole> proposition;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ----------- Thematic Roles -------- \n");
        sb.append(proposition.toString(tokens)).append("\n");
        if (!predicates.isEmpty()) {
            sb.append(" ----------- Semantic Analysis ----- \n");
            predicates.forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }
}
