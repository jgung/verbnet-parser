package io.github.semlink.semlink.aligner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnFrame;
import io.github.semlink.parser.Proposition;
import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.Alignment;
import io.github.semlink.semlink.PbVnMappings.MappedRoleset;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.verbnet.type.FramePhrase;
import io.github.semlink.verbnet.type.SyntacticFrame;
import io.github.semlink.verbnet.type.ThematicRoleType;
import io.github.semlink.verbnet.type.VerbNetSyntaxType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

/**
 * PropBank Verbnet candidate alignment context.
 *
 * @author jgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class PbVnAlignment {

    @Delegate
    private Alignment<PropBankPhrase, FramePhrase> alignment;
    private List<PropBankPhrase> propbankPhrases;
    private SyntacticFrame frame;
    private List<MappedRoleset> rolesets;
    private VnFrame syntaxArgDesc;
    private Proposition<VnClass, PropBankArg> proposition;
    private MappedRoleset roleset;

    public List<PropBankPhrase> byNumber(@NonNull ArgNumber number) {
        return propbankPhrases.stream()
            .filter(s -> s.getNumber() == number)
            .collect(Collectors.toList());
    }

    public <T extends FramePhrase> List<T> bySyntacticType(@NonNull VerbNetSyntaxType type) {
        //noinspection unchecked
        return (List<T>) frame.phrases(type);
    }

    public Optional<FramePhrase> byRole(@NonNull ThematicRoleType role) {
        return frame.role(role);
    }

    @Override
    public String toString() {
        return alignment.toString();
    }

}
