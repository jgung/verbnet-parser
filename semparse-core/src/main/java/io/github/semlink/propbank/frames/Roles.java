package io.github.semlink.propbank.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;


/**
 * PropBank collection of {@link Role roles}.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "roles")
public class Roles implements Serializable, List<Role> {

    private static final long serialVersionUID = 1L;

    @Delegate
    @XmlElement(name = "role", required = true)
    protected List<Role> roles = new ArrayList<>();

    @XmlElement(name = "note")
    private List<String> notes = new ArrayList<>();

    private transient Roleset roleset;

}
