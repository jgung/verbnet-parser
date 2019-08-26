package io.github.semlink.verbnet;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Polar restriction.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class Restriction<T> {

    private boolean include;
    private T type;

    @Override
    public String toString() {
        return (include ? "+" : "-") + type.toString();
    }

}
