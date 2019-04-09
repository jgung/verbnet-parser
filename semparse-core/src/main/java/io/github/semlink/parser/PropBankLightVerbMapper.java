package io.github.semlink.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.clearwsd.DefaultSensePrediction;
import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.tfnlp.app.Span;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.util.TsvUtils;
import io.github.semlink.verbnet.VerbNet;
import io.github.semlink.verbnet.VerbNetClass;
import lombok.NonNull;

/**
 * Light verb proposition mapper.
 *
 * @author jgung
 */
public class PropBankLightVerbMapper {

    private Map<String, Map<String, VerbNetClass>> mappings;
    private DefaultSemanticRoleLabeler<PropBankArg> semanticRoleLabeler;

    public PropBankLightVerbMapper(Map<String, Map<String, VerbNetClass>> mappings,
                                   DefaultSemanticRoleLabeler<PropBankArg> semanticRoleLabeler) {
        this.mappings = mappings;
        this.semanticRoleLabeler = semanticRoleLabeler;
    }

    public Optional<Proposition<VerbNetClass, PropBankArg>> mapProp(@NonNull DepTree tree,
                                                                    @NonNull Proposition<VerbNetClass, PropBankArg> prop) {
        List<Span<PropBankArg>> spans = prop.arguments().spans(PropBankArg.fromLabel("AM-PRR"));
        if (spans.isEmpty()) {
            return Optional.empty();
        }

        Span<PropBankArg> predicatingRelation = spans.get(0);
        Optional<SensePrediction<VerbNetClass>> mapping = getMapping(tree, predicatingRelation, prop.relSpan());
        return mapping.map(verbNetClassSensePrediction -> semanticRoleLabeler.parse(tree,
                Collections.singletonList(verbNetClassSensePrediction)).get(0));
    }

    private Optional<SensePrediction<VerbNetClass>> getMapping(DepTree tree, Span<PropBankArg> prr, Span<PropBankArg> relSpan) {
        String verb = relSpan.get(tree).get(0).feature(FeatureType.Lemma);
        String lemma = prr.get(tree).get(0).feature(FeatureType.Lemma);
        VerbNetClass mapping = mappings.getOrDefault(verb, Collections.emptyMap()).get(lemma);
        if (null == mapping) {
            return Optional.empty();
        }
        return Optional.of(new DefaultSensePrediction<>(prr.startIndex(), prr.get(tree).stream()
                .map(node -> (String) node.feature(FeatureType.Text))
                .collect(Collectors.joining(" ")), mapping.id().classId(), mapping));
    }

    public static Map<String, Map<String, VerbNetClass>> fromMappingsPath(@NonNull String mappingsPath,
                                                                          @NonNull VerbNet verbNet) {
        try {
            Map<String, Map<String, String>> verbNounClassMap = TsvUtils.tsv2Map(mappingsPath, 0, 1, 2);
            Map<String, Map<String, VerbNetClass>> result = new HashMap<>();
            for (Map.Entry<String, Map<String, String>> entry : verbNounClassMap.entrySet()) {
                Map<String, VerbNetClass> clsMap = new HashMap<>();
                for (Map.Entry<String, String> nounClass : entry.getValue().entrySet()) {
                    verbNet.byId(nounClass.getValue()).ifPresent(vncls -> clsMap.put(nounClass.getKey(), vncls));
                }
                result.put(entry.getKey(), clsMap);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
