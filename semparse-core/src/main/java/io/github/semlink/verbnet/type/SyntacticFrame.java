package io.github.semlink.verbnet.type;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.mit.jverbnet.data.IFrame;
import edu.mit.jverbnet.data.syntax.ISyntaxArgDesc;
import edu.mit.jverbnet.data.syntax.SyntaxArgType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SyntacticFrame {

    private List<FramePhrase> elements = new ArrayList<>();
    private Map<ThematicRoleType, FramePhrase> roles = new HashMap<>();
    private ListMultimap<VerbNetSyntaxType, FramePhrase> typeMap = LinkedListMultimap.create();
    private IFrame frame;

    public static SyntacticFrame of(@NonNull IFrame frame) {
        SyntacticFrame result = new SyntacticFrame();
        result.frame = frame;

        result.addElements(frame.getSyntax().getPreVerbDescriptors());
        result.elements.add(new FramePhrase(VerbNetSyntaxType.VERB));
        result.addElements(frame.getSyntax().getPostVerbDescriptors());

        int index = 0;
        for (FramePhrase phrase : result.elements) {
            phrase.index(index++);
            result.typeMap.put(phrase.type(), phrase);
        }

        return result;
    }

    public Optional<FramePhrase> role(@NonNull ThematicRoleType roleType) {
        return Optional.ofNullable(roles.get(roleType));
    }

    public List<FramePhrase> phrases(@NonNull VerbNetSyntaxType type) {
        return typeMap.get(type);
    }

    private void addElements(List<ISyntaxArgDesc> descList) {
        Optional<Preposition> preposition = Optional.empty();
        for (ISyntaxArgDesc syntaxElement : descList) {
            FramePhrase element = FramePhrase.of(syntaxElement);

            preposition.ifPresent(prep -> {
                if (syntaxElement.getType() == SyntaxArgType.NP) {
                    ((NounPhrase) element).preposition(prep);
                } else {
                    elements.add(prep);
                }
            });

            preposition = Optional.empty();

            if (element instanceof Preposition) {
                preposition = Optional.of((Preposition) element);
            } else {
                if (element instanceof NounPhrase) {
                    this.roles.put(((NounPhrase) element).thematicRoleType(), element);
                }
                elements.add(element);
            }
        }

        preposition.ifPresent(elements::add);
    }


}
