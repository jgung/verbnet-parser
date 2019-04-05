package io.github.clearwsd.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.mit.jverbnet.data.IVerbClass;
import io.github.clearwsd.DefaultSensePrediction;
import io.github.clearwsd.DefaultSensePredictor;
import io.github.clearwsd.SensePrediction;
import io.github.clearwsd.propbank.type.PropBankArg;
import io.github.clearwsd.semlink.PbVnMapping;
import io.github.clearwsd.semlink.PbVnMappings;
import io.github.clearwsd.semlink.PropBankVerbNetAligner;
import io.github.clearwsd.semlink.aligner.PbVnAlignment;
import io.github.clearwsd.tfnlp.app.Chunking;
import io.github.clearwsd.tfnlp.app.ShallowParser;
import io.github.clearwsd.tfnlp.app.ShallowParserUtils;
import io.github.clearwsd.tfnlp.type.ITokenSequence;
import io.github.clearwsd.type.DefaultNlpFocus;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.verbnet.VerbNet;
import io.github.clearwsd.verbnet.VerbNetClass;
import io.github.clearwsd.verbnet.semantics.SemanticPredicate;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Semantic role labeling system.
 *
 * @author jgung
 */
@Slf4j
public class SemanticRoleLabeler<A> {

    private ShallowParser shallowParser;
    private Function<String, A> argMapper;
    private Function<NlpFocus<DepNode, DepTree>, ITokenSequence> inputAdapter = RoleLabelerUtils::focus2Sequence;


    public SemanticRoleLabeler(@NonNull ShallowParser shallowParser, @NonNull Function<String, A> argMapper) {
        this.shallowParser = shallowParser;
        this.argMapper = argMapper;
    }

    public <T> List<Proposition<T, A>> parse(@NonNull DepTree tree, @NonNull List<SensePrediction<T>> predicates) {

        List<NlpFocus<DepNode, DepTree>> foci = predicates.stream()
                .map(rel -> new DefaultNlpFocus<>(rel.index(), tree.get(rel.index()), tree))
                .collect(Collectors.toList());

        if (foci.isEmpty()) {
            return Collections.emptyList();
        }

        List<ITokenSequence> seqs = foci.stream().map(inputAdapter).collect(Collectors.toList());
        List<Chunking<A>> chunkings = shallowParser.shallowParseBatch(seqs).stream()
                .map(chunks -> ShallowParserUtils.mapChunks(chunks, argMapper))
                .collect(Collectors.toList());

        Iterator<SensePrediction<T>> senses = predicates.iterator();
        return chunkings.stream()
                .map(chunking -> new Proposition<>(senses.next(), chunking))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws IOException {
        String mappingsPath = "src/main/resources/pbvn-mappings.json";
        String modelDir = "src/main/resources/models/unified-propbank";
        String wsdModel = "models/nlp4j-verbnet-3.3.bin";

        SemanticRoleLabeler<PropBankArg> roleLabeler = new SemanticRoleLabeler<>(
                RoleLabelerUtils.shallowSemanticParser(modelDir), PropBankArg::fromLabel);
        DefaultSensePredictor<IVerbClass> predictor = DefaultSensePredictor.loadFromResource(wsdModel, new Nlp4jDependencyParser());

        VerbNet verbNet = new VerbNet();

        PbVnMappings mappings = new PbVnMappings(PbVnMapping.fromJson(new FileInputStream(mappingsPath)));
        PropBankVerbNetAligner aligner = new PropBankVerbNetAligner(verbNet, mappings);
        SemanticParser parser = new SemanticParser();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print(">> ");
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("quit")) {
                    break;
                }

                List<String> tokens = predictor.tokenize(line);
                DepTree parsed = predictor.parse(tokens);
                List<Proposition<IVerbClass, PropBankArg>> propositions = roleLabeler.parse(parsed, predictor.predict(parsed));

                for (Proposition<IVerbClass, PropBankArg> prop : propositions) {
                    if (prop.predicate().sense() == null) {
                        continue;
                    }
                    Proposition<VerbNetClass, PropBankArg> vclsProp =
                            new Proposition<>(new DefaultSensePrediction<>(prop.predicate().index(),
                                    prop.predicate().originalText(), prop.predicate().id(),
                                    verbNet.byId(prop.predicate().id()).orElse(null)), prop.arguments());
                    System.out.println(vclsProp.predicate().sense().verbClass().getID());

                    Optional<PbVnAlignment> align = aligner.align(vclsProp, parsed);

                    if (align.isPresent()) {
                        System.out.println(" ----------- Semantic Roles -------- ");
                        System.out.println(align.get());
                        List<SemanticPredicate> predicates = parser.parsePredicates(align.get());
                        System.out.println(" ----------- Semantic Analysis ----- ");
                        predicates.forEach(System.out::println);
                    } else {
                        System.out.println("\n" + prop.toString(tokens) + "\n");
                    }
                    System.out.println("\n\n");
                }

            } catch (Exception e) {
                log.warn("An unexpected error occurred", e);
            }
        }
    }

}
