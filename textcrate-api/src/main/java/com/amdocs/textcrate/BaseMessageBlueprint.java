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
import java.util.Objects;

/**
 * <p>A straightforward implementation of {@link MessageBlueprint}.</p>
 *
 * @author evitaliy
 * @since 18 Oct 17
 */
class BaseMessageBlueprint implements MessageBlueprint {

    private final CodeBlueprint codeBlueprint;
    private final Formatting formatting;
    private final Map<String, String> properties;

    BaseMessageBlueprint(CodeBlueprint codeBlueprint, Formatting formatting, Map<String, String> properties) {
        this.codeBlueprint = codeBlueprint;
        this.formatting = formatting;
        this.properties = properties;
    }

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

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseMessageBlueprint that = (BaseMessageBlueprint) o;
        return Objects.equals(codeBlueprint, that.codeBlueprint) && Objects.equals(formatting, that.formatting)
            && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeBlueprint, formatting, properties);
    }

    @Override
    public String toString() {
        return "BaseMessageBlueprint {codeBlueprint=" + codeBlueprint + ", formatting=" + formatting
            + ", properties=" + properties + '}';
    }

    static class Formatting {

        private final String pattern;
        private final Formatter formatter;

        Formatting(String pattern, Formatter formatter) {
            this.pattern = pattern;
            this.formatter = formatter;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Formatting that = (Formatting) o;
            return Objects.equals(pattern, that.pattern) && Objects.equals(formatter, that.formatter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pattern, formatter);
        }

        @Override
        public String toString() {
            return "Formatting {pattern=" + pattern + ", formatter=" + formatter + '}';
        }
    }
}
