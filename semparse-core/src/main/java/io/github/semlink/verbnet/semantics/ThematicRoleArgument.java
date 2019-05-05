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

package io.github.semlink.verbnet.semantics;

import java.util.Optional;

import io.github.semlink.util.StringUtils;
import io.github.semlink.verbnet.type.SemanticArgumentType;
import io.github.semlink.verbnet.type.ThematicRoleType;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Thematic role semantic argument.
 *
 * @author jgung
 */
@Slf4j
@Getter
@Accessors(fluent = true)
public class ThematicRoleArgument<T> extends VariableSemanticArgument<T> {

    public ThematicRoleArgument(@NonNull String value) {
        super(SemanticArgumentType.THEMROLE, value);
        Optional<ThematicRoleType> type = ThematicRoleType.fromString(value);
        if (!type.isPresent()) {
            log.warn("Unrecognized thematic role: {}", value);
            type = Optional.of(ThematicRoleType.NONE);
        }
        this.thematicRoleType = type.get();
    }

    private ThematicRoleType thematicRoleType;

    @Override
    public String toString() {
        return StringUtils.capitalized(thematicRoleType) + "(" + (variable == null ? "?" : variable.toString()) + ")";
    }
}
