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

package io.github.semlink;

import io.github.clearwsd.parser.Nlp4jDependencyParser;
import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.verbnet.DefaultVnIndex;
import io.github.clearwsd.verbnet.VnIndex;
import io.github.semlink.parser.LightVerbMapper;
import io.github.semlink.parser.SemanticRoleLabeler;
import io.github.semlink.parser.VerbNetParse;
import io.github.semlink.parser.VerbNetParser;
import io.github.semlink.parser.VerbNetSenseClassifier;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.VerbNetAligner;

import static io.github.semlink.parser.VerbNetParser.pbRoleLabeler;

/**
 * Just example usage of the {@link io.github.semlink.parser.VerbNetParser} facade.
 *
 * @author jgung
 */
public class VerbNetParserTest {

    public static void main(String[] args) {
        // VerbNet index over VerbNet classes/frames
        VnIndex verbNet = new DefaultVnIndex();

        // Dependency parser used for WSD model and alignment logic
        NlpParser dependencyParser = new Nlp4jDependencyParser();
        // WSD model for predicting VerbNet classes (uses ClearWSD and the NLP4J parser)
        VerbNetSenseClassifier classifier = VerbNetSenseClassifier.fromModelPath("semparse/nlp4j-verbnet-3.3.bin",
                verbNet, dependencyParser);
        // PropBank semantic role labeler from a TF NLP saved model
        SemanticRoleLabeler<PropBankArg> roleLabeler = pbRoleLabeler("semparse/propbank-srl");
        // maps nominal predicates with light verbs to VerbNet classes (e.g. take a bath -> dress-41.1.1)
        LightVerbMapper verbMapper = LightVerbMapper.fromMappingsPath("semparse/lvm.tsv", verbNet);
        // aligner that uses PropBank VerbNet mappings and heuristics to align PropBank roles with VerbNet thematic roles
        VerbNetAligner aligner = VerbNetAligner.of("semparse/pbvn-mappings.json", "semparse/unified-frames.bin");

        // simplifying facade over the above components
        VerbNetParser parser = new VerbNetParser(classifier, roleLabeler, aligner, verbMapper);

        VerbNetParse parse = parser.parse("John ate an apple");
        System.out.println(parse); // Take In[EVENT(E1 = VnClassXml(verbNetId=eat-39.1)), Agent(A0[John]), Patient(A1[an apple])]
    }

}
