package io.github.semlink.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DepTree;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Aggregate of data output during semantic parsing.
 *
 * @author jgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class VerbNetSemanticParse {

    private DepTree tree;
    private List<String> tokens;
    private List<VerbNetProp> props = new ArrayList<>();

    @Override
    public String toString() {
        return props.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n\n"));
    }
}
