package io.github.semlink.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Utilities for shallow parsing/chunking.
 *
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShallowParserUtils {

    public enum Tag {

        BEGIN("B"),
        END("E"),
        SINGLE("S"),
        IN("I"),
        OUT("O");

        @Getter
        @Accessors(fluent = true)
        private String prefix;

        Tag(String prefix) {
            this.prefix = prefix;
        }

        static Tag of(@NonNull String text) {
            for (Tag tag : Tag.values()) {
                if (tag.prefix.equals(text.toUpperCase())) {
                    return tag;
                }
            }
            throw new IllegalArgumentException("Invalid tag: " + text);
        }

    }

    public static <A> Chunking<A> mapChunks(@NonNull Chunking<String> chunking, @NonNull Function<String, A> labelMapper) {
        return new DefaultChunking<>(chunking.spans().stream()
                .map(span -> Span.convert(span, labelMapper.apply(span.label())))
                .collect(Collectors.toList()));
    }

    public static List<Span<String>> tags2Spans(@NonNull List<String> labels) {
        List<Span<String>> spans = new ArrayList<>();

        Tag lastTag = Tag.OUT;
        Tag currTag;
        String lastLabel = "";
        String currLabel;
        int start = 0;
        for (int index = 0, size = labels.size(); index < size; ++index) {
            String label = labels.get(index);
            currLabel = getType(label);
            currTag = getTag(label);

            if (start >= 0 && end(lastTag, currTag, lastLabel, currLabel)) {
                spans.add(new Span<>(lastLabel, start, index - 1));
                start = -1;
            }

            if (start(lastTag, currTag, lastLabel, currLabel)) {
                start = index;
            }

            lastTag = currTag;
            lastLabel = currLabel;
        }

        if (start >= 0 && !Tag.OUT.prefix.equals(lastLabel)) {
            spans.add(new Span<>(lastLabel, start, labels.size() - 1));
        }

        return spans;
    }

    private static String getType(@NonNull String label) {
        int index = label.indexOf("-");
        if (index > 0) {
            return label.substring(index + 1);
        }
        return label;
    }

    private static Tag getTag(@NonNull String label) {
        int index = label.indexOf("-");
        if (index > 0) {
            return Tag.of(label.substring(0, index));
        } else if (label.equals(Tag.OUT.prefix())) {
            return Tag.OUT;
        }
        return Tag.IN;
    }

    private static boolean start(@NonNull Tag prevTag, @NonNull Tag currTag, @NonNull String prevType, @NonNull String currType) {
        if (prevTag.equals(Tag.BEGIN) && currTag.equals(Tag.BEGIN)) {
            return true;
        }
        if (prevTag.equals(Tag.IN) && currTag.equals(Tag.BEGIN)) {
            return true;
        }
        if (prevTag.equals(Tag.OUT) && currTag.equals(Tag.BEGIN)) {
            return true;
        }
        if (prevTag.equals(Tag.OUT) && currTag.equals(Tag.IN)) {
            return true;
        }
        if (prevTag.equals(Tag.SINGLE) && (currTag.equals(Tag.BEGIN)
                || currTag.equals(Tag.SINGLE))) {
            return true;
        }
        if (currTag.equals(Tag.SINGLE)) {
            return true;
        }
        if (prevTag.equals(Tag.END) && currTag.equals(Tag.END)) {
            return true;
        }
        if (prevTag.equals(Tag.END) && currTag.equals(Tag.IN)) {
            return true;
        }
        if (prevTag.equals(Tag.OUT) && currTag.equals(Tag.END)) {
            return true;
        }
        return !currTag.equals(Tag.OUT) && !prevType.equals(currType);
    }

    private static boolean end(@NonNull Tag prevPart, @NonNull Tag currPart, @NonNull String prevType, @NonNull String currType) {
        if (prevPart.equals(Tag.BEGIN) && currPart.equals(Tag.BEGIN)) {
            return true;
        }
        if (prevPart.equals(Tag.BEGIN) && currPart.equals(Tag.OUT)) {
            return true;
        }
        if (prevPart.equals(Tag.IN) && currPart.equals(Tag.BEGIN)) {
            return true;
        }
        if (prevPart.equals(Tag.SINGLE) || currPart.equals(Tag.SINGLE)) {
            return true;
        }
        if (prevPart.equals(Tag.END) && currPart.equals(Tag.END)) {
            return true;
        }
        if (prevPart.equals(Tag.END) && currPart.equals(Tag.IN)) {
            return true;
        }
        if (prevPart.equals(Tag.END) && currPart.equals(Tag.OUT)) {
            return true;
        }
        if (prevPart.equals(Tag.IN) && currPart.equals(Tag.OUT)) {
            return true;
        }
        return !prevPart.equals(Tag.OUT) && !prevType.equals(currType);
    }


}
