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

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.stream.Collectors;

import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.semlink.PropBankPhrase;
import lombok.NonNull;

/**
 * Remove reference arguments from consideration in alignment.
 *
 * @author jgung
 */
public class FilterReferenceAligner implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {
        Multimap<ArgNumber, PropBankPhrase> phraseMap = Multimaps.index(alignment.propbankPhrases().stream()
                .filter(arg -> !arg.isReference())
                .collect(Collectors.toList()), PropBankPhrase::getNumber);
        alignment.propbankPhrases(alignment.propbankPhrases().stream()
                .filter(phrase -> !phrase.isReferenceOrContinuation() || !phraseMap.containsKey(phrase.getNumber()))
                .collect(Collectors.toList()));
    }

}
