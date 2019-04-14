package io.github.semlink.verbnet.type;

import io.github.clearwsd.verbnet.syntax.VnLex;
import io.github.clearwsd.verbnet.syntax.VnNounPhrase;
import io.github.clearwsd.verbnet.syntax.VnPrep;
import io.github.clearwsd.verbnet.syntax.VnSyntax;
import io.github.clearwsd.verbnet.syntax.VnSyntaxType;
import io.github.semlink.semlink.AlignPhrase;
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

    public static FramePhrase of(@NonNull VnSyntax desc) {
        if (desc.type() == VnSyntaxType.NP) {
            return NounPhrase.of((VnNounPhrase) desc);
        } else if (desc.type() == VnSyntaxType.PREP) {
            return Preposition.of((VnPrep) desc);
        } else if (desc.type() == VnSyntaxType.LEX) {
            return LexicalElement.of((VnLex) desc);
        }
        return new FramePhrase(VerbNetSyntaxType.valueOf(desc.type().name()));
    }

    @Override
    public String toString() {
        return type.name();
    }

}
