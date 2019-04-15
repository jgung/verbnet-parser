package io.github.semlink.type;

import java.util.EnumSet;

import lombok.NonNull;

/**
 * Base unit used for NLP processing.
 *
 * @author jgung
 */
public interface HasFields {

    /**
     * Return the value for a given field on this token.
     *
     * @param key field key
     * @param <T> field type
     * @return field value
     */
    <T> T field(@NonNull String key);


    /**
     * Return the value for a given field on this token.
     *
     * @param key field key
     * @param <T> field type
     * @return field value
     */
    <T> T field(@NonNull Enum key);

    boolean hasFields(@NonNull EnumSet<?> keys);

    boolean hasFields(@NonNull String... keys);

}
