package io.github.semlink.propbank.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * PropBank collection of {@link PbRole roles}.
 *
 * @author jgung
 */
@Data
@EqualsAndHashCode(exclude = "roleset")
@ToString(exclude = "roleset")
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "roles")
public class Roles implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "role", required = true)
    protected List<PbRole> roles = new ArrayList<>();

    @XmlElement(name = "note")
    private List<String> notes = new ArrayList<>();

    private transient Roleset roleset;

}
