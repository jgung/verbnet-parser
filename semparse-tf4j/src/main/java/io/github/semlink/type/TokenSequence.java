package io.github.semlink.type;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

/**
 * Default {@link ITokenSequence} implementation.
 *
 * @author jgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class TokenSequence extends Fields implements ITokenSequence {

    @Delegate
    private List<IToken> tokens;

}
