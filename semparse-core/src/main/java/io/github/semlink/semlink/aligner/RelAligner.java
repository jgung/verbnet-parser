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

package io.github.semlink.semlink.aligner;

import com.google.common.base.Preconditions;
import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.verbnet.type.FramePhrase;
import io.github.semlink.verbnet.type.VerbNetSyntaxType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * Aligns verb/PropBank rel.
 *
 * @author jgung
 */
public class RelAligner implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {
        List<PropBankPhrase> rels = alignment.byNumber(ArgNumber.V).stream()
            .filter(rel -> !rel.isContinuation())
            .filter(rel -> !rel.isReference()).collect(Collectors.toList());
        List<FramePhrase> verbs = alignment.bySyntacticType(VerbNetSyntaxType.VERB);
        Preconditions.checkState(rels.size() == 1 && verbs.size() == 1);
        alignment.add(rels.get(0), verbs.get(0));
    }

}
