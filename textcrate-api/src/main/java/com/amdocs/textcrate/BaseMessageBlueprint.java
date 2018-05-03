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
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>A straightforward implementation of {@link MessageBlueprint}.</p>
 *
 * @author evitaliy
 * @since 18 Oct 17
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
class BaseMessageBlueprint implements MessageBlueprint {

    private final CodeBlueprint codeBlueprint;
    private final Formatting formatting;
    private final Map<String, String> properties;

    @Override
    public String format(Object[] arguments) {
        return formatting.formatter.format(formatting.pattern, arguments);
    }

    @Override
    public String getPattern() {
        return formatting.pattern;
    }

    @Override
    public String getCode() {
        return codeBlueprint.getCode();
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class Formatting {
        private final String pattern;
        private final Formatter formatter;
    }
}
