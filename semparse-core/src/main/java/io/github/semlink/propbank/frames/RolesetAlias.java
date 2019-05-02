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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.semlink.util.LowerCaseEnumAdapter;
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

    private transient Roleset roleset;

    public enum AliasPos {
        V, N, A, L
    }

    private static final class PersonAdapter extends LowerCaseEnumAdapter<AliasPos> {

        PersonAdapter() {
            super(AliasPos.class, null);
        }
    }

}
