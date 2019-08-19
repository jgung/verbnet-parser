package io.github.semlink.clearwsd;

import io.github.clearwsd.app.WordSenseCLI;
import io.github.clearwsd.parser.NlpParser;
import io.github.semlink.app.DependencyParser;
import lombok.Setter;

/**
 * TF-NLP {@link WordSenseCLI}.
 *
 * @author jamesgung
 */
public class TfNlpCli extends WordSenseCLI {

    @Setter
    private String parserPath = "/home/jamesgung/Downloads/dep-model";

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
