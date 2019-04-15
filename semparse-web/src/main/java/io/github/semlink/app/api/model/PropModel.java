package io.github.semlink.app.api.model;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.semlink.parser.VerbNetProp;
import io.github.semlink.app.Chunking;
import io.github.semlink.app.Span;
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
    private List<SpanModel> propBankSpans = new ArrayList<>();
    private List<SpanModel> verbNetSpans = new ArrayList<>();

    public PropModel(VerbNetProp prop) {
        if (prop.propbankProp().predicate().sense() != null) {
            this.sense = prop.propbankProp().predicate().sense().verbNetId().toString();
        } else {
            this.sense = prop.propbankProp().predicate().id();
        }
        List<SemanticPredicateModel> predicates = prop.predicates().stream()
            .map(SemanticPredicateModel::new)
            .collect(Collectors.toList());
        ListMultimap<String, SemanticPredicateModel> predsByEvent = LinkedListMultimap.create();
        for (SemanticPredicateModel pred: predicates) {
            predsByEvent.put(pred.eventName(), pred);
        }
        predsByEvent.keySet().stream().sorted(String::compareTo)
            .forEach(key -> events.add(new EventModel().name(key).predicates(predsByEvent.get(key))));

        this.propBankSpans = getSpans(prop.propbankProp().arguments(), prop.tokens(), prop.propbankProp().predicate().index());

        if (prop.verbnetProp() != null) {
            this.verbNetSpans = getSpans(prop.verbnetProp().arguments(), prop.tokens(), prop.verbnetProp().predicate().index());
            // add modifiers to VerbNet spans
            this.verbNetSpans = addModifiers(verbNetSpans, propBankSpans);
        }
    }

    private static List<SpanModel> addModifiers(List<SpanModel> verbNetSpans, List<SpanModel> propBankSpans) {
        Map<Integer, SpanModel> pbSpanMap = Maps.uniqueIndex(verbNetSpans, SpanModel::start);
        List<SpanModel> updatedSpans = new ArrayList<>();
        for (SpanModel pbSpan : propBankSpans) {
            if (!pbSpanMap.containsKey(pbSpan.start())) {
                if (pbSpan.label().startsWith("AM")) {
                    updatedSpans.add(pbSpan);
                }
            } else {
                updatedSpans.add(pbSpanMap.get(pbSpan.start()));
            }
        }
        return updatedSpans;
    }

    private static List<SpanModel> getSpans(Chunking<?> chunking, List<String> tokens, int predIndex) {
        return chunking.spans().stream()
                .map(span -> new SpanModel()
                        .start(span.startIndex())
                        .end(span.endIndex())
                        .label(span.label().toString())
                        .text(coveredText(span, tokens))
                        .isPredicate(predIndex == span.startIndex()))
                .collect(Collectors.toList());
    }

    private static String coveredText(Span<?> span, List<String> tokens) {
        return span.get(tokens).stream().map(Object::toString).collect(Collectors.joining(" "));
    }

}
