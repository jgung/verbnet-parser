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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.verbnet.type.PrepType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Aligner utilities.
 *
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlignmentUtils {

    /**
     * Return the head from a phrase of {@link DepNode dependency nodes}.
     *
     * @param nodeList dep node phrase
     * @return head node in the phrase
     */
    public static DepNode getHead(@NonNull List<DepNode> nodeList) {
        int start = nodeList.get(0).index();
        int end = Iterables.getLast(nodeList).index();
        for (DepNode node : nodeList) {
            if (node.isRoot()) {
                return node;
            }
            if (node.head().index() < start || node.head().index() > end) {
                return node;
            }
        }
        return nodeList.get(0);
    }

    /**
     * Returns whether or not a given phrase a clause.
     */
    public static boolean isClause(@NonNull List<DepNode> phrase) {
        DepNode node = getHead(phrase);
        Set<String> clauseLabels = ImmutableSet.of("advcl", "acl", "csubj", "ccomp", "xcomp");
        String label = node.feature(FeatureType.Dep);
        return clauseLabels.contains(label);
    }

    /**
     * Return the {@link PrepType} if present from a given {@link PropBankPhrase}.
     */
    public static Optional<PrepType> getPrep(@NonNull List<DepNode> tokens) {
        String startText = tokens.get(0).feature(FeatureType.Text);
        if (tokens.size() > 1) {
            // e.g. "out of" or "in between"
            String concatenated = startText + "_" + tokens.get(1).feature(FeatureType.Text);
            try {
                return Optional.of(PrepType.valueOf(concatenated.toUpperCase()));
            } catch (Exception ignored) {
            }
        }
        try {
            return Optional.of(PrepType.valueOf(startText.toUpperCase()));
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

}
