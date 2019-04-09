package io.github.semlink.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Front-end prediction services.
 *
 * @author jgung
 */
@SpringBootConfiguration
@Import({PredictionConfiguration.class})
@SpringBootApplication
public class VerbNetPredictionService {

    public static void main(String[] args) {
        SpringApplication.run(VerbNetPredictionService.class, args);
    }

}
