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

import java.util.Optional;

import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.verbnet.type.FramePhrase;
import io.github.semlink.verbnet.type.NounPhrase;
import io.github.semlink.verbnet.type.PrepType;
import io.github.semlink.verbnet.type.Preposition;
import lombok.NonNull;

import static io.github.semlink.semlink.aligner.AlignmentUtils.getPrep;

/**
 * Aligner based on syntactic cues/restrictions.
 *
 * @author jgung
 */
public class SynResAligner implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {
        for (PropBankPhrase pbPhrase : alignment.sourcePhrases()) {
            filterInvalid(alignment, pbPhrase);
        }
    }

    private void filterInvalid(@NonNull PbVnAlignment alignment, @NonNull PropBankPhrase pbPhrase) {
        Optional<PrepType> prep = getPrep(pbPhrase.tokens());
        if (!prep.isPresent()) {
            return;
        }
        if (alignment.alignedSource(pbPhrase)) {
            for (FramePhrase phrase : alignment.alignedPhrases(pbPhrase)) {
                if (phrase instanceof NounPhrase && ((NounPhrase) phrase).preposition().isPresent()) {
                    Preposition preposition = ((NounPhrase) phrase).preposition().get();
                    if (!preposition.valid().isEmpty() && !preposition.valid().contains(prep.get())) {
                        alignment.remove(pbPhrase, phrase);
                    }
                }
            }
        }
    }

}
