package io.github.semlink.verbnet.type;

import com.google.common.base.Strings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.mit.jverbnet.data.syntax.ISyntaxArgDesc;
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

    public static Preposition of(@NonNull ISyntaxArgDesc prepArgDesc) {
        Preposition preposition = new Preposition();
        if (!Strings.isNullOrEmpty(prepArgDesc.getValue())) {
            preposition.valid(PrepType.fromString(prepArgDesc.getValue()));
        }

        prepArgDesc.getSelRestrictions()
                .getTypeRestrictions()
                .forEach((key, value) -> PrepSelRelType.fromString(key.getID()).ifPresent(restr -> {
                    if (value) {
                        preposition.include().add(restr);
                    } else {
                        preposition.exclude().add(restr);
                    }
                }));
        return preposition;
    }

    @Override
    public String toString() {
        return type() + "[" + valid().stream().map(Enum::name).collect(Collectors.joining(" | ")) + "]";
    }
}