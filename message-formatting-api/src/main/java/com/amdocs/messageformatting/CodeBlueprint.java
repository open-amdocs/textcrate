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

package com.amdocs.messageformatting;

import com.amdocs.messageformatting.api.Formatter;
import java.util.Objects;

/**
 * <p>Blueprint of a message code. It can be reused for all messages of the same type (i.e. same pattern, code and
 * formatting rules).</p>
 *
 * @author evitaliy
 * @since 24 Jan 18
 */
class CodeBlueprint {

    private final int id;
    private final Formatting formatting;

    CodeBlueprint(int id, Formatting formatting) {
        this.id = id;
        this.formatting = formatting;
    }

    public String getCode() {
        return formatting.formatter.format(formatting.pattern, formatting.offset + id);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CodeBlueprint that = (CodeBlueprint) o;
        return id == that.id && Objects.equals(formatting, that.formatting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, formatting);
    }

    @Override
    public String toString() {
        return "CodeBlueprint {id=" + id + ", formatting=" + formatting + '}';
    }

    static class Formatting {

        private final int offset;
        private final String pattern;
        private final Formatter formatter;

        Formatting(int offset, String pattern, Formatter formatter) {
            this.offset = offset;
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

            Formatting formatting = (Formatting) o;
            return offset == formatting.offset && Objects.equals(pattern, formatting.pattern)
                && Objects.equals(formatter, formatting.formatter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(offset, pattern, formatter);
        }

        @Override
        public String toString() {
            return "Formatting {offset=" + offset + ", pattern=" + pattern + ", formatter=" + formatter + '}';
        }
    }

}
