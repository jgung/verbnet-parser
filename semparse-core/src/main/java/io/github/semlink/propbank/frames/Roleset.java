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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.semlink.propbank.type.ArgNumber;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * PropBank roleset.
 *
 * @author jgung
 */
@Data
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "roleset")
public class Roleset implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String id;

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;

    @XmlElementWrapper(name = "aliases")
    @XmlElement(name = "alias")
    private List<RolesetAlias> aliases = new ArrayList<>();

    @XmlElement(name = "note")
    private List<String> notes = new ArrayList<>();

    @XmlElement(name = "roles", required = true)
    private Roles roles;

    @XmlElement(name = "example")
    private List<Example> examples = new ArrayList<>();

    private transient Predicate predicate;

    public Optional<PbRole> getRole(@NonNull ArgNumber argNumber) {
        return roles.roles().stream().filter(role -> role.number() == argNumber).findFirst();
    }

}
