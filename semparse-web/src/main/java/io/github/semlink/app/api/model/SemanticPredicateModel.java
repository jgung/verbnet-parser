/*
 * Copyright 2019 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.semlink.app.api.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.FeatureType;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.util.StringUtils;
import io.github.semlink.verbnet.semantics.EventArgument;
import io.github.semlink.verbnet.semantics.SemanticArgument;
import io.github.semlink.verbnet.semantics.SemanticPredicate;
import io.github.semlink.verbnet.semantics.ThematicRoleArgument;
import io.github.semlink.verbnet.semantics.VerbSpecificArgument;
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
    private String predicateType;
    private List<SemanticArgumentModel> args = new ArrayList<>();
    private boolean polarity = true;

    public SemanticPredicateModel(String eventName, SemanticPredicate predicate) {
        this.eventName = eventName;
        this.predicate = predicate.toString();
        this.predicateType = StringUtils.capitalized(predicate.type());
        for (SemanticArgument argument : predicate.arguments()) {
            SemanticArgumentModel model = new SemanticArgumentModel(argument);
            if (argument instanceof EventArgument && eventName.equals(model.value)) {
                // remove redundant event argument
                continue;
            }

            args.add(model);
        }
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

}
