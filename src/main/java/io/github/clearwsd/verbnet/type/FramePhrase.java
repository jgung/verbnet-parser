package io.github.clearwsd.verbnet.type;

import edu.mit.jverbnet.data.syntax.ISyntaxArgDesc;
import edu.mit.jverbnet.data.syntax.SyntaxArgType;
import io.github.clearwsd.semlink.AlignPhrase;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class FramePhrase extends AlignPhrase {

    @Setter
    private int order;
    private VerbNetSyntaxType type;

    public FramePhrase(@NonNull VerbNetSyntaxType type) {
        super(0);
        this.type = type;
    }

    public static FramePhrase of(@NonNull ISyntaxArgDesc desc) {
        if (desc.getType() == SyntaxArgType.NP) {
            return NounPhrase.of(desc);
        } else if (desc.getType() == SyntaxArgType.PREP) {
            return Preposition.of(desc);
        } else if (desc.getType() == SyntaxArgType.LEX) {
            return LexicalElement.of(desc);
        }
        return new FramePhrase(VerbNetSyntaxType.valueOf(desc.getType().name()));
    }

    @Override
    public String toString() {
        return type.name();
    }

}
