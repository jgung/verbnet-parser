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

import io.github.semlink.util.LowerCaseEnumAdapter;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * {@link Example} inflection.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "alias")
public class Inflection implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "person", required = true)
    @XmlJavaTypeAdapter(PersonAdapter.class)
    private Person person = Person.NS;

    @XmlAttribute(name = "tense", required = true)
    @XmlJavaTypeAdapter(TenseAdapter.class)
    private Tense tense = Tense.NS;

    @XmlAttribute(name = "aspect", required = true)
    @XmlJavaTypeAdapter(AspectAdapter.class)
    private Aspect aspect = Aspect.NS;

    @XmlAttribute(name = "voice", required = true)
    @XmlJavaTypeAdapter(VoiceAdapter.class)
    private Voice voice = Voice.NS;

    @XmlAttribute(name = "form", required = true)
    @XmlJavaTypeAdapter(FormAdapter.class)
    private Form form = Form.NS;

    public enum Person {
        THIRD,
        OTHER,
        NS
    }

    public enum Tense {
        PRESENT,
        PAST,
        FUTURE,
        NS
    }

    public enum Aspect {
        PERFECT,
        PROGRESSIVE,
        BOTH,
        NS
    }

    public enum Voice {
        ACTIVE,
        PASSIVE,
        NS
    }

    public enum Form {
        INFINITIVE,
        GERUND,
        PARTICIPLE,
        FULL,
        NS
    }

    private static final class PersonAdapter extends LowerCaseEnumAdapter<Person> {

        PersonAdapter() {
            super(Person.class, Person.NS);
        }
    }

    private static final class TenseAdapter extends LowerCaseEnumAdapter<Tense> {

        TenseAdapter() {
            super(Tense.class, Tense.NS);
        }
    }

    private static final class AspectAdapter extends LowerCaseEnumAdapter<Aspect> {

        AspectAdapter() {
            super(Aspect.class, Aspect.NS);
        }
    }

    private static final class VoiceAdapter extends LowerCaseEnumAdapter<Voice> {

        VoiceAdapter() {
            super(Voice.class, Voice.NS);
        }
    }

    private static final class FormAdapter extends LowerCaseEnumAdapter<Form> {

        FormAdapter() {
            super(Form.class, Form.NS);
        }
    }


}
