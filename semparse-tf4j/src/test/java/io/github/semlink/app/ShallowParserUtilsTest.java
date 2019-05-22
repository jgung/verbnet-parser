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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * Shallow parsing utilities test.
 *
 * @author jgung
 */
public class ShallowParserUtilsTest {

    @Test
    public void spans2Tags$2Spans() {
        List<Span<String>> spans = Arrays.asList(
                new Span<>("PER", 1, 2),
                new Span<>("ORG", 4, 4)
        );
        List<String> strings = ShallowParserUtils.spans2Tags(spans, Objects::toString, 6);
        assertEquals(Arrays.asList("O", "B-PER", "I-PER", "O", "B-ORG", "O"), strings);
    }

    @Test
    public void spans2Tags$Contiguous() {
        List<Span<String>> spans = Arrays.asList(
                new Span<>("PER", 1, 2),
                new Span<>("PER", 3, 3),
                new Span<>("PER", 4, 4)
        );
        List<String> strings = ShallowParserUtils.spans2Tags(spans, Objects::toString, 6);
        assertEquals(Arrays.asList("O", "B-PER", "I-PER", "B-PER", "B-PER", "O"), strings);
    }

}