package io.github.clearwsd.util;

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
        return Arrays.stream(value.name().split("_"))
                .map(s -> s.length() == 1 ? s.toUpperCase() : s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

}
