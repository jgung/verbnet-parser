package io.github.semlink.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.semlink.parser.DefaultSemanticRoleLabeler;
import io.github.semlink.parser.PropBankLightVerbMapper;
import io.github.semlink.parser.VerbNetSemanticParser;
import io.github.semlink.parser.VerbNetSenseClassifier;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.PropBankVerbNetAligner;
import io.github.semlink.verbnet.VerbNet;

import static io.github.semlink.app.util.JarExtractionUtil.resolveDirectory;
import static io.github.semlink.app.util.JarExtractionUtil.resolveFile;
import static io.github.semlink.parser.VerbNetSemanticParser.roleLabeler;

/**
 * Prediction configuration file.
 *
 * @author jgung
 */
@Configuration
public class PredictionConfiguration {

    @Value("${verbnet.demo.pbvn-mappings-path:pbvn-mappings.json}")
    private String mappingsPath = "pbvn-mappings.json";
    @Value("${verbnet.demo.wsd-mode-path:models/verbnet/nlp4j-verbnet-3.3.bin}")
    private String wsdModel = "models/verbnet/nlp4j-verbnet-3.3.bin";
    @Value("${verbnet.demo.srl-model-path:models/unified-propbank}")
    private String srlModelDir = "models/unified-propbank/";
    @Value("${verbnet.demo.lvm-path:lvm.tsv}")
    private String lvmPath = "lvm.tsv";

    @Bean
    public VerbNet verbNet() {
        return new VerbNet();
    }

    @Bean
    public VerbNetSenseClassifier verbNetSenseClassifier(@Autowired VerbNet verbNet) {
        String wsdModel = resolveFile(this.wsdModel);
        return VerbNetSenseClassifier.fromModelPath(wsdModel, verbNet);
    }

    @Bean
    public VerbNetSemanticParser verbNetSemanticParser(@Autowired VerbNetSenseClassifier verbNetSenseClassifier,
                                                       @Autowired VerbNet verbNet) {
        String mappingsPath = resolveFile(this.mappingsPath);
        String modelDir = resolveDirectory(this.srlModelDir);
        String lvmPath = resolveFile(this.lvmPath);

        DefaultSemanticRoleLabeler<PropBankArg> roleLabeler = roleLabeler(modelDir);

        PropBankVerbNetAligner aligner = PropBankVerbNetAligner.of(mappingsPath);

        PropBankLightVerbMapper mapper = new PropBankLightVerbMapper(PropBankLightVerbMapper.fromMappingsPath(lvmPath, verbNet),
            roleLabeler);

        return new VerbNetSemanticParser(verbNetSenseClassifier, roleLabeler, aligner, mapper);
    }

}
