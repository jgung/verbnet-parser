package io.github.semlink.parser;

import java.util.ArrayList;
import java.util.List;

import io.github.clearwsd.verbnet.VnClass;
import io.github.semlink.semlink.SemlinkRole;
import io.github.semlink.verbnet.semantics.SemanticPredicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * VerbNet proposition.
 *
 * @author jgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class VerbNetProp {

    private int tokenIndex;
    private List<String> tokens;
    private List<SemanticPredicate> predicates = new ArrayList<>();
    private Proposition<VnClass, SemlinkRole> proposition;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ----------- Thematic Roles -------- \n");
        sb.append(proposition.toString(tokens)).append("\n");
        if (!predicates.isEmpty()) {
            sb.append(" ----------- Semantic Analysis ----- \n");
            predicates.forEach(p -> sb.append(p).append("\n"));
        }
        return sb.toString();
    }
}
