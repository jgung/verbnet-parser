package io.github.clearwsd.verbnet.type;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import edu.mit.jverbnet.data.syntax.ISyntaxArgDesc;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true)
public class NounPhrase extends FramePhrase {

    @Getter
    private ThematicRoleType thematicRoleType = ThematicRoleType.NONE;
    @Getter
    private Set<NounPhraseSynRelType> include = new HashSet<>();
    @Getter
    private Set<NounPhraseSynRelType> exclude = new HashSet<>();

    private Preposition preposition;

    public NounPhrase() {
        super(VerbNetSyntaxType.NP);
    }

    public Optional<Preposition> preposition() {
        return Optional.ofNullable(preposition);
    }

    public static NounPhrase of(@NonNull ISyntaxArgDesc argDesc) {
        NounPhrase nounPhrase = new NounPhrase();

        ThematicRoleType.fromString(argDesc.getValue())
                .ifPresent(nounPhrase::thematicRoleType);

        argDesc.getSelRestrictions()
                .getTypeRestrictions()
                .forEach((key, value) -> NounPhraseSynRelType.fromString(key.getID()).ifPresent(restr -> {
                    if (value) {
                        nounPhrase.include().add(restr);
                    } else {
                        nounPhrase.exclude().add(restr);
                    }
                }));

        return nounPhrase;
    }

    @Override
    public String toString() {
        String result = type() + "[" + thematicRoleType.name() + "]";
        if (preposition().isPresent()) {
            result = preposition.toString() + " " + result;
        }
        return result;
    }
}
