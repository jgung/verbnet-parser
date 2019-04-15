package io.github.semlink.app.api.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.util.StringUtils;
import io.github.semlink.verbnet.semantics.EventArgument;
import io.github.semlink.verbnet.semantics.SemanticArgument;
import io.github.semlink.verbnet.semantics.SemanticPredicate;
import io.github.semlink.verbnet.semantics.ThematicRoleArgument;
import io.github.semlink.verbnet.semantics.VerbSpecificArgument;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON-serializable model for VerbNet semantic predicates.
 *
 * @author jgung
 */
@Slf4j
@Setter
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SemanticPredicateModel {

    private String eventName;
    private String predicate;
    private String type;
    private List<SemanticArgumentModel> args = new ArrayList<>();
    private boolean polarity = true;

    public SemanticPredicateModel(SemanticPredicate predicate) {
        this.eventName = getEventName(predicate);
        this.predicate = predicate.toString();
        this.type = StringUtils.capitalized(predicate.type());
        this.args = predicate.arguments().stream()
            .map(SemanticArgumentModel::new)
            .collect(Collectors.toList());
        this.polarity = predicate.polarity();
    }

    @Setter
    @Getter
    @Accessors(fluent = true)
    @NoArgsConstructor
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class SemanticArgumentModel {

        private String type;
        private String value;

        public SemanticArgumentModel(SemanticArgument argument) {
            this.type = StringUtils.capitalized(argument.type());
            if (argument instanceof ThematicRoleArgument) {
                Object variable = ((ThematicRoleArgument) argument).variable();
                if (variable instanceof PropBankPhrase) {
                    this.value = ((PropBankPhrase) variable).span()
                        .get(((PropBankPhrase) variable).parse()).stream()
                        .map(s -> (String) s.feature(FeatureType.Text))
                        .collect(Collectors.joining(" "));
                } else {
                    this.value = "";
                }
                this.type = ((ThematicRoleArgument) argument).thematicRoleType().toString();
            } else if (argument instanceof EventArgument) {
                this.value = ((EventArgument) argument).id();
                if (this.value.equals("E")) {
                    this.value = "E1";
                }
                this.type = StringUtils.capitalized(((EventArgument) argument).relation());
            } else if (argument instanceof VerbSpecificArgument) {
                Object variable = ((VerbSpecificArgument) argument).variable();
                if (null != variable) {
                    this.type = argument.value();
                    this.value = variable.toString();
                } else {
                    this.value = argument.value();
                }
            } else {
                this.value = argument.value();
            }
        }
    }

    public static String getEventName(SemanticPredicate predicate) {
        return predicate.arguments().stream()
            .filter(e -> e instanceof EventArgument)
            .map(e -> ((EventArgument) e).id())
            .map(i -> {
                if ("E".equals(i)) {
                    return "E1";
                }
                return i;
            })
            .min(String::compareTo).orElse("E1");
    }

}
