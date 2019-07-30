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

package io.github.semlink.extractor;

import org.tensorflow.example.SequenceExample;

import java.util.Optional;

import io.github.semlink.type.HasFields;
import lombok.NonNull;

/**
 * {@link SequenceExample extractor}
 *
 * @author jamesgung
 */
public interface SequenceExampleExtractor {

    /**
     * Extract a sequence example for input to a TF saved model.
     *
     * @param sequence input sequence
     * @return TF sequence example proto
     */
    SequenceExample extractSequence(@NonNull HasFields sequence);

    Optional<Vocabulary> vocabulary(@NonNull String key);

}
