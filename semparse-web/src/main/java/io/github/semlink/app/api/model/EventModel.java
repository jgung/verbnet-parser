package io.github.semlink.app.api.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

}
