package io.github.clearwsd.propbank.frames;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.clearwsd.util.LowerCaseEnumAdapter;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * PropBank {@link Roleset} alias.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "alias")
public class RolesetAlias implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "pos", required = true)
    @XmlJavaTypeAdapter(PersonAdapter.class)
    private AliasPos pos;

    @XmlAttribute(name = "framenet")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String framenet;

    @XmlAttribute(name = "verbnet")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String verbnet;

    @XmlValue
    protected String lemma;

    public enum AliasPos {
        V, N, A, L
    }

    private static final class PersonAdapter extends LowerCaseEnumAdapter<AliasPos> {
        PersonAdapter() {
            super(AliasPos.class, null);
        }
    }

}
