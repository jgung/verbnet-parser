package io.github.clearwsd.parser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.clearwsd.semlink.PropBankPhrase;
import io.github.clearwsd.semlink.aligner.PbVnAlignment;
import io.github.clearwsd.verbnet.VerbNetClass;
import io.github.clearwsd.verbnet.semantics.EventArgument;
import io.github.clearwsd.verbnet.semantics.SemanticPredicate;
import io.github.clearwsd.verbnet.semantics.ThematicRoleArgument;
import io.github.clearwsd.verbnet.type.FramePhrase;
import io.github.clearwsd.verbnet.type.SemanticArgumentType;
import lombok.NonNull;

/**
 * VerbNet semantic parser.
 *
 * @author jgung
 */
public class SemanticParser {

    public List<SemanticPredicate> parsePredicates(@NonNull PbVnAlignment proposition) {
        List<SemanticPredicate> predicates = proposition.frame().frame().getSemantics().getPredicates().stream()
                .map(SemanticPredicate::of)
                .collect(Collectors.toList());

        for (SemanticPredicate predicate : predicates) {
            List<EventArgument<VerbNetClass>> args = predicate.get(SemanticArgumentType.EVENT);
            for (EventArgument<VerbNetClass> arg : args) {
                arg.variable(proposition.proposition().predicate().sense());
            }

            List<ThematicRoleArgument<PropBankPhrase>> roles = predicate.get(SemanticArgumentType.THEMROLE);
            for (ThematicRoleArgument<PropBankPhrase> role : roles) {
                Optional<FramePhrase> phrase = proposition.byRole(role.thematicRoleType());
                phrase.ifPresent(framePhrase -> role.variable(proposition.alignment().getSource(framePhrase)));
            }

        }

        return predicates;
    }

}
