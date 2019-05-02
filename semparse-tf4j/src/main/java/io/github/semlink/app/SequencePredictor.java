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

package io.github.semlink.app;

import java.util.List;

import lombok.NonNull;

/**
 * Sequence prediction interface.
 *
 * @author jgung
 */
public interface SequencePredictor<T> {

    /**
     * Predict labels for a given input sequence.
     *
     * @param input target input
     * @return list of labels
     */
    List<String> predict(@NonNull T input);

    /**
     * Predict labels for a given batch of input sequences.
     *
     * @param inputs input sequence batch
     * @return batched sequence labels
     */
    List<List<String>> predictBatch(@NonNull List<T> inputs);

}
