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
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.semlink.propbank.type.ArgNumber;
import io.github.semlink.propbank.type.FunctionTag;
import io.github.semlink.util.LowerCaseEnumAdapter;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * PropBank role.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "f", required = true)
    @XmlJavaTypeAdapter(FunctionTagAdapter.class)
    private FunctionTag functionTag;

    @XmlAttribute(name = "n", required = true)
    @XmlJavaTypeAdapter(NumberAdapter.class)
    private ArgNumber number;

    @XmlAttribute(name = "descr", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String description;

    @XmlElement(name = "vnrole", required = true)
    private List<VerbNetRole> verbNetRoles = new ArrayList<>();

    @XmlElement(name = "fnrole", required = true)
    private List<FrameNetRole> frameNetRoles = new ArrayList<>();

    private transient Roleset roleset;

    private static final class NumberAdapter extends XmlAdapter<String, ArgNumber> {

        @Override
        public ArgNumber unmarshal(String value) {
            return ArgNumber.valueOf("A" + value.toUpperCase());
        }

        @Override
        public String marshal(ArgNumber value) {
            return value.name().substring(1);
        }
    }

    private static final class FunctionTagAdapter extends LowerCaseEnumAdapter<FunctionTag> {

        FunctionTagAdapter() {
            super(FunctionTag.class, null);
        }
    }

}
