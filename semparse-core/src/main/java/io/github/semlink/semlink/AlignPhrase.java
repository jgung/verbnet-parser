package io.github.semlink.semlink;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Phrase within an alignment.
 *
 * @author jgung
 */
@Getter
@Setter
@EqualsAndHashCode(of = "index")
@Accessors(fluent = true)
@AllArgsConstructor
public class AlignPhrase {

    /**
     * Unique index within sequence.
     */
    private int index;

}
