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

import java.util.List;
import java.util.stream.Collectors;

import io.github.semlink.verbnet.semantics.Event;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Event model.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class EventModel {

    private String name;

    private List<SemanticPredicateModel> predicates;

    public EventModel(@NonNull Event event) {
        this.name = event.event.id();
        this.predicates = event.predicates().stream().map(SemanticPredicateModel::new).collect(Collectors.toList());
        predicates.forEach(p -> p.eventName(name));
    }

}
