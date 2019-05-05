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

package io.github.semlink.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.clearwsd.verbnet.DefaultVnIndex;
import io.github.clearwsd.verbnet.VnIndex;
import io.github.semlink.parser.LightVerbMapper;
import io.github.semlink.parser.SemanticRoleLabeler;
import io.github.semlink.parser.VerbNetParser;
import io.github.semlink.parser.VerbNetSenseClassifier;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.VerbNetAligner;

import static io.github.semlink.app.util.JarExtractionUtil.resolveDirectory;
import static io.github.semlink.app.util.JarExtractionUtil.resolveFile;
import static io.github.semlink.parser.VerbNetParser.pbRoleLabeler;

/**
 * Prediction configuration file.
 *
 * @author jgung
 */
@Configuration
public class PredictionConfiguration {

    @Value("${verbnet.demo.pbvn-mappings-path:pbvn-mappings.json}")
    private String mappingsPath = "pbvn-mappings.json";
    @Value("${verbnet.demo.wsd-mode-path:models/nlp4j-verbnet-3.3.bin}")
    private String wsdModel = "models/nlp4j-verbnet-3.3.bin";
    @Value("${verbnet.demo.srl-model-path:models/unified-propbank}")
    private String srlModelDir = "models/unified-propbank/";
    @Value("${verbnet.demo.lvm-path:lvm.tsv}")
    private String lvmPath = "lvm.tsv";
    @Value("${verbnet.demo.pb-path:propbank/unified-frames.bin}")
    private String pbPath = "propbank/unified-frames.bin";

    @Bean
    public VnIndex verbNet() {
        return new DefaultVnIndex();
    }

    @Bean
    public VerbNetSenseClassifier verbNetSenseClassifier(@Autowired VnIndex verbNet) {
        String wsdModel = resolveFile(this.wsdModel);
        return VerbNetSenseClassifier.fromModelPath(wsdModel, verbNet);
    }

    @Bean
    public VerbNetParser verbNetSemanticParser(@Autowired VerbNetSenseClassifier verbNetSenseClassifier,
                                               @Autowired VnIndex verbNet) {
        String mappingsPath = resolveFile(this.mappingsPath);
        String modelDir = resolveDirectory(this.srlModelDir);
        String lvmPath = resolveFile(this.lvmPath);
        String pbPath = resolveFile(this.pbPath);

        SemanticRoleLabeler<PropBankArg> roleLabeler = pbRoleLabeler(modelDir);

        VerbNetAligner aligner = VerbNetAligner.of(mappingsPath, pbPath);

        LightVerbMapper mapper = LightVerbMapper.fromMappingsPath(lvmPath, verbNet);

        return new VerbNetParser(verbNetSenseClassifier, roleLabeler, aligner, mapper);
    }

}
