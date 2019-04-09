package io.github.semlink.propbank.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * VerbNet role mapping.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "vnrole")
public class VerbNetRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "vncls", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String verbNetClass;

    @XmlAttribute(name = "vntheta", required = true)
    @XmlJavaTypeAdapter(CapitalizeAdapter.class)
    protected String thematicRole;

    public static class CapitalizeAdapter extends XmlAdapter<String, String> {

        @Override
        public String unmarshal(String value) {
            List<String> result = new ArrayList<>();
            for (String token : value.split("_")) {
                result.add(token.substring(0, 1).toUpperCase() + token.substring(1).toLowerCase());
            }
            return String.join("_", result);
        }

        @Override
        public String marshal(String value) {
            return value;
        }
    }

}
