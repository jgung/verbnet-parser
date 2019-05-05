# SemParse

SemParse provides common utilities for interfacing with lexical resources in the [SemLink](https://verbs.colorado.edu/semlink/)
project, as well as tools for parsing and extraction.
This includes a VerbNet semantic parser, which produces VerbNet thematic roles and semantic predicates.

See a demo of the VerbNet parser in action [here](http://verbnet-semantic-parser.appspot.com/)!

### Requirements
* [Java 8](http://www.oracle.com/technetwork/java/javase/overview/index.html) and [Apache Maven](https://maven.apache.org/)

## semparse-core
Provides a VerbNet parser that uses [VerbNet](http://verbs.colorado.edu/~mpalmer/projects/verbnet.html) class predictions and 
[PropBank](https://propbank.github.io/) semantic roles to align to a VerbNet frame and produce VerbNet semantic representations.

To use the parser, you will need to download and unzip the [pre-trained models and mapping files](https://drive.google.com/open?id=12krafgEoEVKFue9zZxj2zkPxIQH1pLV4).

The API is a work in progress (as the project itself is not stable), but an overview of current usage is shown here:
```java
import io.github.clearwsd.parser.*;
import io.github.clearwsd.verbnet.*;
import io.github.semlink.parser.*;
import io.github.semlink.propbank.type.PropBankArg;
import io.github.semlink.semlink.VerbNetAligner;

import static io.github.semlink.parser.VerbNetParser.pbRoleLabeler;

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

```

## semparse-tf4j
Wrapper for Tensorflow Java API to load and make predictions with TF-based NLP sequence models exported as saved models from [TF-NLP](https://github.com/jgung/tf-nlp). 

## semparse-web
A [Spring Boot](https://spring.io/projects/spring-boot) web app with a [React](https://reactjs.org/) frontend to demonstrate VerbNet parsing models.

To build and run the demo yourself, you'll need to copy the pre-trained models and mapping files into the resources folder:
```bash
# (download and unzip models as described above into semparse/ directory)
cd verbnet-parser
cp -R semparse/* semparse-web/src/main/resources/
mvn clean install -DskipTests
cd semparse-web
mvn spring-boot:run
```
Then just open [localhost:8080](http://localhost:8080) in your browser.

[Try the demo here!](http://verbnet-semantic-parser.appspot.com/)