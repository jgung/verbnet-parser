package io.github.semlink.util;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    public static String capitalized(@NonNull Enum<?> value) {
        return capitalized(value.name());
    }

    public static String capitalized(@NonNull String name) {
        return Arrays.stream(name.split("_"))
            .map(s -> s.length() == 1 ? s.toUpperCase() : s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

}
