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
