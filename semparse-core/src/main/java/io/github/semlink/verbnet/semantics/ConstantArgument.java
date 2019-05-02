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

import io.github.semlink.verbnet.type.SemanticArgumentType;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Constant-valued semantic argument.
 *
 * @author jgung
 */
@Slf4j
@Getter
@Accessors(fluent = true)
public class ConstantArgument extends SemanticArgument {

    public ConstantArgument(@NonNull String value) {
        super(SemanticArgumentType.CONSTANT, value);
    }

}
