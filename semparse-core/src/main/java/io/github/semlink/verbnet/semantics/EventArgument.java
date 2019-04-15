package io.github.semlink.verbnet.semantics;

import com.google.common.base.Strings;
import io.github.semlink.verbnet.type.SemanticArgumentType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Event argument.
 *
 * @author jgung
 */
@Slf4j
@Getter
@Accessors(fluent = true)
public class EventArgument<T> extends VariableSemanticArgument<T> {

    public static final Pattern EVENT_PATTERN = Pattern.compile("(during|end|start|result)?(\\(E\\d?\\)|E\\d?)",
        Pattern.CASE_INSENSITIVE);

    public EventArgument(@NonNull String value) {
        super(SemanticArgumentType.EVENT, value);
        Matcher matcher = EVENT_PATTERN.matcher(value);
        if (matcher.find()) {
            String rel = matcher.group(1);
            if (!Strings.isNullOrEmpty(rel)) {
                relation = EventRelation.valueOf(matcher.group(1).toUpperCase());
            }
            id = matcher.group(2).toUpperCase();
        } else {
            log.warn("Failed to parse event argument: {}", value);
        }
    }

    public enum EventRelation {
        EVENT,
        START,
        DURING,
        END,
        RESULT
    }

    private EventRelation relation = EventRelation.EVENT;
    private String id = "E1";

    @Override
    public String toString() {
        return relation.toString() + "(" + id + (variable == null ? "" : " = " + variable.toString()) + ")";
    }
}
