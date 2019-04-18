package io.github.semlink.app.api.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;
import java.util.stream.Collectors;

import io.github.semlink.app.Span;
import io.github.semlink.propbank.frames.PbRole;
import io.github.semlink.semlink.SemlinkRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Labeled span model.
 *
 * @author jgung
 */
@Setter
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SemlinkRoleModel {

    private int index = 0;
    private boolean isPredicate;
    private String label;
    private int start;
    private int end;
    private String text;

    private String pb;
    private String vn;
    private String description;

    public SemlinkRoleModel(@NonNull Span<SemlinkRole> span, @NonNull List<String> tokens, int predIndex) {
        start = span.startIndex();
        end = span.endIndex();
        label = span.label().toString();
        text = coveredText(span, tokens);
        isPredicate = predIndex == span.startIndex();
        pb = span.label().pb().map(Object::toString).orElse("");
        vn = span.label().vn().map(Object::toString).orElse("");
        description = span.label().definition().map(PbRole::description).orElse("");
    }

    private static String coveredText(Span<?> span, List<String> tokens) {
        return span.get(tokens).stream().map(Object::toString).collect(Collectors.joining(" "));
    }
}
