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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Phrase alignment, with multiple targets potentially mapping to the same source.
 *
 * @author jgung
 */
@Accessors(fluent = true)
@NoArgsConstructor
public class Alignment<S extends AlignPhrase, T extends AlignPhrase> {

    private ListMultimap<S, T> alignment = LinkedListMultimap.create();

    @Getter
    private Set<S> sourcePhrases = new LinkedHashSet<>();
    @Getter
    private Set<T> targetPhrases = new LinkedHashSet<>();

    public static <S extends AlignPhrase, T extends AlignPhrase> Alignment<S, T> of(@NonNull List<S> sourcePhrases,
                                                                                    @NonNull List<T> targetPhrases) {
        Alignment<S, T> result = new Alignment<>();
        result.sourcePhrases.addAll(sourcePhrases);
        result.targetPhrases.addAll(targetPhrases);
        return result;
    }

    public S getSource(@NonNull T target) {
        for (Map.Entry<S, T> entry : alignment.entries()) {
            if (entry.getValue().equals(target)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Alignment<S, T> add(@NonNull S source, @NonNull T target) {
        alignment.put(source, target);
        return this;
    }

    public Alignment<S, T> remove(@NonNull S source, @NonNull T target) {
        alignment.remove(source, target);
        return this;
    }

    public boolean alignedSource(@NonNull S source) {
        return alignment.containsKey(source);
    }

    public boolean alignedTarget(@NonNull T target) {
        return alignment.values().contains(target);
    }

    public List<S> sourcePhrases(boolean aligned) {
        if (aligned) {
            return sourcePhrases.stream().filter(this::alignedSource).collect(Collectors.toList());
        }
        return sourcePhrases.stream().filter(src -> !alignedSource(src)).collect(Collectors.toList());
    }

    public List<T> targetPhrases(boolean aligned) {
        if (aligned) {
            return targetPhrases.stream().filter(this::alignedTarget).collect(Collectors.toList());
        }
        return targetPhrases.stream().filter(src -> !alignedTarget(src)).collect(Collectors.toList());
    }

    public List<T> alignedPhrases(@NonNull S source) {
        return alignment.get(source);
    }

    @Override
    public String toString() {
        List<String> lines = new ArrayList<>();

        Set<T> allAligned = new HashSet<>();

        for (S source : sourcePhrases) {
            Collection<T> aligned = alignment.get(source);
            allAligned.addAll(aligned);

            String target = aligned.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(" ... "));

            lines.add(String.format("%-20s -> %-20s", source, target.isEmpty() ? "" : target));
        }

        Sets.difference(targetPhrases, allAligned).forEach(notAligned ->
                lines.add(String.format("%-20s <- %-20s", "", notAligned)));

        return String.join("\n", lines);
    }

}
