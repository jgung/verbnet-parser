package io.github.semlink.propbank.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * PropBank predicate.
 */
@Data
@EqualsAndHashCode(of = {"lemma", "rolesets"})
@ToString(of = "lemma")
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "predicate")
public class Predicate implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "lemma", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lemma;

    @XmlElement(name = "roleset", required = true)
    protected List<Roleset> rolesets = new ArrayList<>();

    private transient Frameset frameset;

}
