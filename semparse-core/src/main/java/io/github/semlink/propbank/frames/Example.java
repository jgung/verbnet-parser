/*
 * Copyright 2019 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.semlink.propbank.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * PropBank {@link Roleset} example.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "example")
public class Example implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;

    @XmlAttribute(name = "src", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String source;

    @XmlElement(name = "inflection")
    private Inflection inflection;

    @XmlElement(name = "note")
    private List<String> notes = new ArrayList<>();

    @XmlElement(name = "text", required = true)
    private String exampleText;

    @XmlElementRef
    private List<ExampleNode> children = new ArrayList<>();

    private transient Roleset roleset;

    public List<ExampleArgument> arguments() {
        return children.stream()
            .filter(node -> node instanceof ExampleArgument)
            .map(node -> (ExampleArgument) node)
            .collect(Collectors.toList());
    }

    public Optional<ExampleRelation> relation() {
        return children.stream()
            .filter(node -> node instanceof ExampleRelation)
            .findFirst()
            .map(node -> (ExampleRelation) node);
    }

    /**
     * {@link Example} node, either rel or argument.
     *
     * @author jgung
     */
    @Data
    @Accessors(fluent = true)
    public static abstract class ExampleNode implements Serializable {

        private static final long serialVersionUID = 1L;

        @XmlValue
        protected String text;

    }

    /**
     * {@link Example} argument.
     *
     * @author jgung
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "arg")
    public static class ExampleArgument extends ExampleNode {

        private static final long serialVersionUID = 1L;
        @XmlAttribute(name = "f")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String functionTag;
        @XmlAttribute(name = "n", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        private String number;

    }

    /**
     * {@link Example} relation.
     *
     * @author jgung
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "rel")
    public static class ExampleRelation extends ExampleNode {

        private static final long serialVersionUID = 1L;

        /**
         * A rel can have an "f" attribute for a single reason, so that auxiliary uses of the verb "have" can be marked as such.
         * There should be no other "f" attributes.
         */
        @XmlAttribute(name = "f")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String functionTag;

    }

}
