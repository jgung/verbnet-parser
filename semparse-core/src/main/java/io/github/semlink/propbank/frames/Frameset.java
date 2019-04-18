package io.github.semlink.propbank.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * PropBank frame file.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "frameset")
public class Frameset implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "predicate", required = true)
    protected List<Predicate> predicates = new ArrayList<>();

    public List<Roleset> rolesets() {
        return predicates.stream()
                .map(Predicate::rolesets)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
