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

package io.github.semlink.tensor;

import org.tensorflow.Tensor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

/**
 * {@link AutoCloseable} wrapper for lists of {@link Tensor tensors}.
 *
 * @author jgung
 */
@AllArgsConstructor
public class TensorList implements List<Tensor<?>>, AutoCloseable {

    public static TensorList of(List<Tensor<?>> tensors) {
        if (tensors instanceof TensorList) {
            return (TensorList) tensors;
        }
        return new TensorList(tensors);
    }

    @Delegate
    private List<Tensor<?>> tensors;

    @Override
    public void close() {
        tensors.forEach(Tensor::close);
    }

}
