package io.github.semlink.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Extractor that returns values for a given key on inputs.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
public abstract class KeyExtractor<T> implements Extractor<T> {

    @Setter
    protected List<Function<String, String>> mappingFunctions = new ArrayList<>();

    private String name;
    protected String key;

    protected KeyExtractor(String name, String key) {
        this.name = name;
        this.key = key;
    }

    protected String map(String input) {
        for (Function<String, String> mappingFunction : mappingFunctions) {
            input = mappingFunction.apply(input);
        }
        return input;
    }

}
