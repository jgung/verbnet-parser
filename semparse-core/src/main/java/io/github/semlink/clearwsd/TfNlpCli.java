package io.github.semlink.clearwsd;

import com.beust.jcommander.Parameter;
import io.github.clearwsd.app.WordSenseCLI;
import io.github.clearwsd.parser.NlpParser;
import io.github.semlink.parser.DependencyParser;

/**
 * TF-NLP {@link WordSenseCLI}.
 *
 * @author jamesgung
 */
public class TfNlpCli extends WordSenseCLI {

    @Parameter(names = {"--parser"}, description = "Path to TF-based dependency parser")
    private String parserPath;

    public TfNlpCli(String[] args) {
        super(args);
    }

    @Override
    protected NlpParser parser() {
        return new TfNlpParser(DependencyParser.fromDirectory(parserPath));
    }

    public static void main(String[] args) {
        new TfNlpCli(args).run();
    }

}
