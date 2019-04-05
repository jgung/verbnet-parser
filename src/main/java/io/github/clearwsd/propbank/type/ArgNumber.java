package io.github.clearwsd.propbank.type;

/**
 * PropBank numbered argument.
 *
 * @author jgung
 */
public enum ArgNumber {

    A0,
    A1,
    A2,
    A3,
    A4,
    A5,
    A6,
    AM,
    V;

    public boolean isModifier() {
        return this == AM;
    }

    public boolean isRel() {
        return this == V;
    }

    public boolean isNumber() {
        return !isModifier() && !isRel();
    }

}