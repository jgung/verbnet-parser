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

import java.util.List;

import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.type.DepTree;
import io.github.semlink.verbnet.VnClass;
import lombok.NonNull;

/**
 * VerbNet predicate detector.
 *
 * @author jgung
 */
public interface VnPredicateDetector {

    /**
     * Identify VerbNet sense-tagged predicates from a dependency parse.
     *
     * @param depTree dependency parsed sentence
     * @return predicated predicates and their sense tags
     */
    List<SensePrediction<VnClass>> detectPredicates(@NonNull DepTree depTree);

}
