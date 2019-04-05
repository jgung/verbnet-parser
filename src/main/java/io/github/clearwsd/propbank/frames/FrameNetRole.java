package io.github.clearwsd.propbank.frames;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * FrameNet role mapping.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "fnrole")
public class FrameNetRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "frame", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String frame;

    @XmlAttribute(name = "fe", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String frameElement;

}
