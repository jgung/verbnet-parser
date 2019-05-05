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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import io.github.semlink.app.ShallowParser;
import io.github.semlink.app.TensorflowModel;
import io.github.semlink.type.Fields;
import io.github.semlink.type.HasFields;
import io.github.semlink.type.IToken;
import io.github.semlink.type.ITokenSequence;
import io.github.semlink.type.Token;
import io.github.semlink.type.TokenSequence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static io.github.semlink.app.ShallowParserUtils.Tag.OUT;

/**
 * Semantic role labeling utilities specific to our SRL system's input needs.
 *
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleLabelerUtils {

    private static final String PREDICATE_INDEX_KEY = "predicate_index";
    private static final String MARKER_KEY = "marker";
    private static final String WORD_KEY = "word";
    private static final String LABEL_KEY = "gold";

    /**
     * Convert an {@link NlpFocus} to an {@link ITokenSequence} for use in feature extraction.
     */
    public static ITokenSequence focus2Sequence(NlpFocus<DepNode, DepTree> tree) {
        List<IToken> tokens = new ArrayList<>();
        for (DepNode node : tree.tokens()) {
            Token token = new Token(node.feature(FeatureType.Text), node.index());
            tokens.add(token);
        }
        TokenSequence sequence = new TokenSequence(tokens);
        sequence.add(PREDICATE_INDEX_KEY, tree.focus().index());
        return sequence;
    }

    /**
     * Populate features for a shallow semantic parser.
     *
     * @param tokens input sequence of tokens
     * @return feature fields
     */
    private static HasFields shallowSemParseFeatures(ITokenSequence tokens) {
        List<String> words = tokens.stream()
                .map(IToken::text)
                .collect(Collectors.toList());
        int predicateIndex = tokens.field(PREDICATE_INDEX_KEY);

        Fields features = new Fields();
        features.add(WORD_KEY, words);
        features.add(LABEL_KEY, Collections.nCopies(words.size(), OUT.prefix()));
        features.add(MARKER_KEY, IntStream.range(0, words.size())
                .mapToObj(i -> i == predicateIndex ? String.valueOf(1) : String.valueOf(0))
                .collect(Collectors.toList()));
        features.add(PREDICATE_INDEX_KEY, Collections.singletonList(String.valueOf(predicateIndex)));
        return features;
    }

    /**
     * Initialize a shallow semantic parser from a Tensorflow model at a given directory.
     *
     * @param modelDir Tensorflow saved model directory
     * @return shallow semantic parser
     */
    public static ShallowParser shallowSemanticParser(@NonNull String modelDir) {
        return new ShallowParser(TensorflowModel.fromDirectory(modelDir), RoleLabelerUtils::shallowSemParseFeatures);
    }

}
