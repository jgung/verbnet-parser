package io.github.semlink.verbnet.semantics;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * VerbNet event with associated {@link SemanticPredicate semantic predicates}.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class Event {

    public EventArgument event;

    public List<SemanticPredicate> predicates;

}
