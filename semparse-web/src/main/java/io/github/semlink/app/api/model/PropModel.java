package io.github.semlink.app.api.model;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.semlink.app.Chunking;
import io.github.semlink.parser.VerbNetProp;
import io.github.semlink.semlink.SemlinkRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * JSON-serializable model for VerbNet semantic analyses.
 *
 * @author jgung
 */
@Setter
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PropModel {

    private String sense;
    private List<EventModel> events = new ArrayList<>();
    private List<SemlinkRoleModel> spans = new ArrayList<>();

    public PropModel(VerbNetProp prop) {
        if (prop.proposition().predicate().sense() != null) {
            this.sense = prop.proposition().predicate().sense().verbNetId().toString();
        } else {
            this.sense = prop.proposition().predicate().id();
        }
        List<SemanticPredicateModel> predicates = prop.predicates().stream()
                .map(SemanticPredicateModel::new)
                .collect(Collectors.toList());
        ListMultimap<String, SemanticPredicateModel> predsByEvent = LinkedListMultimap.create();
        for (SemanticPredicateModel pred : predicates) {
            predsByEvent.put(pred.eventName(), pred);
        }
        predsByEvent.keySet().stream().sorted(String::compareTo)
                .forEach(key -> events.add(new EventModel().name(key).predicates(predsByEvent.get(key))));

        this.spans = getSpans(prop.proposition().arguments(), prop.tokens(), prop.proposition().predicate().index());
    }

    private static List<SemlinkRoleModel> getSpans(Chunking<SemlinkRole> chunking, List<String> tokens, int predIndex) {
        return chunking.spans().stream()
                .map(role -> new SemlinkRoleModel(role, tokens, predIndex))
                .collect(Collectors.toList());
    }

}
