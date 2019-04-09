package io.github.semlink.type;

/**
 * Textual token.
 *
 * @author jgung
 */
public interface IToken extends HasFields {

    /**
     * Index of this token within a sequence.
     */
    int index();

    /**
     * Text associated with this token.
     */
    String text();

}
