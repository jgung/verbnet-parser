package io.github.semlink.verbnet.type;

import io.github.clearwsd.verbnet.restrictions.DefaultVnRestrictions;
import io.github.clearwsd.verbnet.syntax.VnPrep;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(fluent = true)
public class Preposition extends FramePhrase {

    private Set<PrepType> valid = Collections.emptySet();
    private Set<PrepSelRelType> include = new HashSet<>();
    private Set<PrepSelRelType> exclude = new HashSet<>();

    public Preposition() {
        super(VerbNetSyntaxType.PREP);
    }

    public static Preposition of(@NonNull VnPrep prepArgDesc) {
        Preposition preposition = new Preposition();
        preposition.valid(prepArgDesc.types().stream().map(PrepType::fromString).collect(Collectors.toSet()));

        List<DefaultVnRestrictions<PrepSelRelType>> restrictions = DefaultVnRestrictions
            .map(prepArgDesc.restrictions(), PrepSelRelType::fromString);
        if (restrictions.size() > 0) {
            preposition.exclude(restrictions.get(0).exclude());
            preposition.include(restrictions.get(0).include());
        }
        return preposition;
    }

    @Override
    public String toString() {
        return type() + "[" + valid().stream().map(Enum::name).collect(Collectors.joining(" | ")) + "]";
    }
}