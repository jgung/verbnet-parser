package io.github.clearwsd.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XML adapter for lowercase enums (adapted from https://stackoverflow.com/a/24257168).
 *
 * @author jgung
 */
public abstract class LowerCaseEnumAdapter<E extends Enum> extends XmlAdapter<String, E> {

    private Class<E> clazz;
    private E defaultValue;

    public LowerCaseEnumAdapter(Class<E> clazz, E defaultValue) {
        this.clazz = clazz;
        this.defaultValue = defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E unmarshal(String value) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return (E) Enum.valueOf(clazz, value.toUpperCase());
    }

    @Override
    public String marshal(E value) {
        if (value == defaultValue) {
            return null;
        }
        return value.name().toLowerCase();
    }
}