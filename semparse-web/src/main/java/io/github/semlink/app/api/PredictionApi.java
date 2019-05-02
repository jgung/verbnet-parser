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

package io.github.semlink.app.api;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.github.clearwsd.verbnet.VnIndex;
import io.github.semlink.app.api.model.SentenceModel;
import io.github.semlink.parser.DefaultSentenceNormalizer;
import io.github.semlink.parser.VerbNetSemanticParse;
import io.github.semlink.parser.VerbNetSemanticParser;
import io.github.semlink.parser.VerbNetSenseClassifier;
import lombok.extern.slf4j.Slf4j;

/**
 * VerbNet prediction API.
 *
 * @author jgung
 */
@Slf4j
@RestController
public class PredictionApi {

    private final VerbNetSenseClassifier verbNetSenseClassifier;
    private final VnIndex verbNet;

    private LoadingCache<String, VerbNetSemanticParse> parseCache;

    @Autowired
    public PredictionApi(VerbNetSemanticParser parser, VerbNetSenseClassifier verbNetSenseClassifier, VnIndex verbNet) {
        this.verbNetSenseClassifier = verbNetSenseClassifier;
        this.verbNet = verbNet;
        parseCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(CacheLoader.from(parser::parseSentence));
    }

    @RequestMapping("/predict/semantics")
    public SentenceModel predictSemantics(@RequestParam(value = "utterance") String utterance) {
        Stopwatch sw = Stopwatch.createStarted();
        List<String> sentences = verbNetSenseClassifier.segment(utterance);

        utterance = sentences.size() > 0 ? sentences.get(0) : utterance;
        utterance = new DefaultSentenceNormalizer().normalize(utterance);

        VerbNetSemanticParse verbNetSemanticParses = parseCache.getUnchecked(utterance);
        log.info("Processed utterance \"{}\" in {}", utterance, sw.stop());

        return new SentenceModel(verbNetSemanticParses);
    }

}
