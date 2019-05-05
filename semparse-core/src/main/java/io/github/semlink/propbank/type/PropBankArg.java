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

package io.github.semlink.propbank.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;


/**
 * PropBank argument.
 *
 * @author jgung
 */
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropBankArg {

    public static final Pattern ROLE_PATTERN = Pattern.compile("^(?:([CR])-)?(V|A(?:RG)?(M|\\d)(?:-(\\S+))?)$");

    private ArgNumber number;
    private FunctionTag functionTag;
    private boolean continuation = false;
    private boolean reference = false;

    public static PropBankArg fromLabel(@NonNull String label) {
        Matcher matcher = ROLE_PATTERN.matcher(label.toUpperCase());

        if (matcher.find()) {
            PropBankArg arg = new PropBankArg();
            if ("V".equals(matcher.group(2))) {
                arg.number = ArgNumber.V;
            } else {
                arg.number = ArgNumber.valueOf("A" + matcher.group(3));
            }
            arg.functionTag = FunctionTag.VSP;

            String function = matcher.group(4);
            if (function != null) {
                arg.functionTag = FunctionTag.fromString(function);
            }

            String type = matcher.group(1);
            arg.continuation = "C".equals(type);
            arg.reference = "R".equalsIgnoreCase(type);
            return arg;
        }

        throw new IllegalArgumentException("Unexpected label: " + label);
    }

    public boolean isModifier() {
        return number.isModifier();
    }

    @Override
    public String toString() {
        String result = number.name();
        if (continuation) {
            result = "C-" + result;
        }
        if (reference) {
            result = "R-" + result;
        }
        if (number.isModifier()) {
            result = result + "-" + functionTag.name();
        }
        return result;
    }

}
