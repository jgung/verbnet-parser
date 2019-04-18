package io.github.semlink.app.api.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.semlink.app.Span;
import io.github.semlink.parser.VerbNetSemanticParse;
import io.github.semlink.semlink.SemlinkRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * JSON-serializable model for VerbNet parsing results.
 *
 * @author jgung
 */
@Setter
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SentenceModel {

    private List<SemlinkRoleModel> tokens = new ArrayList<>();
    private List<PropModel> props = new ArrayList<>();

    public SentenceModel(VerbNetSemanticParse semanticParse) {
        List<SemlinkRoleModel> rels = semanticParse.props().stream().map(p -> {
            Span<SemlinkRole> relSpan = p.proposition().relSpan();
            return new SemlinkRoleModel().start(relSpan.startIndex())
                    .end(relSpan.endIndex())
                    .text(String.join(" ", relSpan.get(semanticParse.tokens())))
                    .label(p.proposition().predicate().id())
                    .isPredicate(true);
        }).collect(Collectors.toList());
        int index = 0;
        int relIndex = 0;
        for (SemlinkRoleModel spanModel : rels) {
            if (spanModel.start() > index) {
                tokens.add(new SemlinkRoleModel()
                        .start(index)
                        .end(spanModel.start() - 1)
                        .isPredicate(false)
                        .text(String.join(" ", semanticParse.tokens().subList(index, spanModel.start()))));
            }
            tokens.add(spanModel.index(relIndex));
            index = spanModel.end() + 1;
            ++relIndex;
        }
        if (index < semanticParse.tokens().size()) {
            tokens.add(new SemlinkRoleModel()
                    .start(index)
                    .end(index)
                    .isPredicate(false)
                    .text(String.join(" ", semanticParse.tokens().subList(index, semanticParse.tokens().size()))));
        }

        this.props = semanticParse.props().stream().map(PropModel::new).collect(Collectors.toList());
    }

}
