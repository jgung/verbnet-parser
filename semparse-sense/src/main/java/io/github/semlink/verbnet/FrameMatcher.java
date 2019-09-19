package io.github.semlink.verbnet;

import io.github.clearwsd.corpus.CoNllDepTreeReader;
import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.semlink.app.DependencyParser;
import io.github.semlink.clearwsd.TfNlpParser;
import io.github.semlink.verbnet.type.NpSynRes;
import io.github.semlink.verbnet.type.SyntacticFrame;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * VerbNet syntactic frame matching tool.
 *
 * @author jamesgung
 */
public class FrameMatcher {

    public static void main(String[] args) {
        DependencyParser model = DependencyParser.fromDirectory("/home/jamesgung/Downloads/dep-model");
        TfNlpParser parser = new TfNlpParser(model);

        VnIndex vnIndex = new DefaultVnIndex();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(">> ");
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                continue;
            } else if (line.equalsIgnoreCase("exit")) {
                break;
            }
            processLine(vnIndex, parser, line);
        }

    }

    public static void processLine(VnIndex index, NlpParser parser, String line) {
        VnSyntaxAnalyzer analyzer = new VnSyntaxAnalyzer();

        List<String> tokens = parser.tokenize(line);

        DepTree parse = parser.parse(tokens);

        String lemma = parse.root().feature(FeatureType.Lemma);
        for (VnClass vnClass : index.getByLemma(lemma)) {
            System.out.println(vnClass.verbNetId());
            for (VnFrame vnFrame : vnClass.framesIncludeInherited()) {
                SyntacticFrame syn = SyntacticFrame.of(vnFrame);
                System.out.println(syn);
            }
            System.out.println();
        }

        System.out.println(CoNllDepTreeReader.treeToString(parse));

        for (DepNode child: parse.root().children()) {
            Set<Restriction<NpSynRes>> restrictions = analyzer.analyzeNp(child);
            if (!restrictions.isEmpty()) {
                System.out.println(
                    child.feature(FeatureType.Text).toString() + "\t"
                        + analyzer.argumentPosition(child) + "\t"
                        + restrictions.stream().map(Objects::toString).collect(Collectors.joining(", ")));
            }
        }

    }

}
