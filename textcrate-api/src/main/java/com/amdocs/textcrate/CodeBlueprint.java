/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amdocs.textcrate;

import com.amdocs.textcrate.api.Formatter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>Blueprint of a message code. It can be reused for all messages of the same type (i.e. same pattern, code and
 * formatting rules).</p>
 *
 * @author evitaliy
 * @since 24 Jan 18
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
class CodeBlueprint {

    private final int id;
    private final Formatting formatting;

    String getCode() {
        return formatting.formatter.format(formatting.pattern, formatting.offset + id);
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class Formatting {
        private final int offset;
        private final String pattern;
        private final Formatter formatter;
    }
}
