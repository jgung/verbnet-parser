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

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DefaultNlpFocus;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import io.github.semlink.app.Chunking;
import io.github.semlink.app.ShallowParser;
import io.github.semlink.app.ShallowParserUtils;
import io.github.semlink.type.ITokenSequence;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Default {@link SemanticRoleLabeler} implementation.
 *
 * @author jgung
 */
@AllArgsConstructor
public class DefaultSemanticRoleLabeler<A> implements SemanticRoleLabeler<A> {

    private ShallowParser shallowParser;
    private Function<String, A> argMapper;
    private Function<NlpFocus<DepNode, DepTree>, ITokenSequence> inputAdapter;

    /**
     * Initialize a semantic role labeler that maps output of a shallow parser to a given argument type using a provided mapping
     * function.
     *
     * @param shallowParser base shallow parser, which produces string labels irrespective of the argument type
     * @param argMapper function mapping labels output by the shallow parser onto the correct argument type
     */
    public DefaultSemanticRoleLabeler(@NonNull ShallowParser shallowParser, @NonNull Function<String, A> argMapper) {
        this(shallowParser, argMapper, RoleLabelerUtils::focus2Sequence);
    }

    @Override
    public List<Proposition<DepNode, A>> parse(@NonNull DepTree tree, @NonNull List<Integer> indices) {
        if (indices.isEmpty()) {
            return Collections.emptyList();
        }

        // (1) map dependency parse tree to an input sequence of features given each predicate
        List<ITokenSequence> featsGivenPredicate = indices.stream()
            .map(predicate -> new DefaultNlpFocus<>(predicate, tree.get(predicate), tree))
            .map(inputAdapter)
            .collect(Collectors.toList());

        // (2) apply shallow parser to feature inputs as a single batch
        List<Chunking<A>> chunkings = shallowParser.shallowParseBatch(featsGivenPredicate).stream()
            .map(chunks -> ShallowParserUtils.mapChunks(chunks, argMapper))
            .collect(Collectors.toList());
        Preconditions.checkState(chunkings.size() == indices.size());

        // (3) map batched predictions to each input proposition
        Iterator<Integer> senses = indices.iterator();
        return chunkings.stream()
            .map(chunking -> {
                int index = senses.next();
                return new Proposition<>(index, tree.get(index), chunking);
            })
            .collect(Collectors.toList());
    }

}
