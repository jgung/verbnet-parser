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

package io.github.semlink.semlink;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.semlink.propbank.type.ArgNumber;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * PropBank role to VerbNet role mapping.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PbVnMapping {

    public static final ObjectMapper OM = new ObjectMapper();

    private String lemma;

    private List<RolesetMapping> mappings = new ArrayList<>();

    @Data
    @Accessors(fluent = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class RolesetMapping {

        private String id;
        private List<RolesMapping> mappings = new ArrayList<>();
    }

    @Data
    @Accessors(fluent = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class RolesMapping {

        private String vncls;
        private Set<MappedRole> roles = new TreeSet<>();
    }

    @Data
    @Accessors(fluent = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class MappedRole implements Comparable<MappedRole> {

        private ArgNumber number;
        private String vntheta;

        @Override
        public int compareTo(MappedRole other) {
            int number = this.number.compareTo(other.number);
            if (number != 0) {
                return number;
            } else {
                return this.vntheta.compareTo(other.vntheta);
            }
        }
    }

    public static List<PbVnMapping> fromJson(@NonNull InputStream inputStream) throws IOException {
        JavaType type = OM.getTypeFactory().constructCollectionType(List.class, PbVnMapping.class);
        return OM.readValue(inputStream, type);
    }

}
