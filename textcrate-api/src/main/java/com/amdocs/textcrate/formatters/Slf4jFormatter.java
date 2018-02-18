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

package com.amdocs.textcrate.formatters;

import com.amdocs.textcrate.api.Formatter;
import com.amdocs.textcrate.api.InvalidPatternException;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.helpers.MessageFormatter;

/**
 * <p>This formatter allows both constructing messages and validating patterns using
 * the <a href="https://www.slf4j.org/index.html">SLF4J</a> format. This can be useful when passing the same messages
 * to the SLF4J API for deferred construction. The implementation calls the fast
 * <a href="https://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html">org.slf4j.helpers.MessageFormatter</a>
 * internally to format messages.</p>
 *
 * <p>Deferring message construction for logging
 * (see <a href="http://slf4j.org/faq.html#logging_performance">What is the fastest way of (not) logging?</a>):</p>
 *
 * @author evitaliy
 * @since 22 Aug 17
 */
public class Slf4jFormatter implements Formatter {

    private static final String PLACEHOLDER = "{}";
    private static final char ESCAPE_CHAR = '\\';
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(\\\\*\\" + PLACEHOLDER + ")");

    @Override
    public String format(String pattern, Object... arguments) {
        return MessageFormatter.arrayFormat(pattern, arguments).getMessage();
    }

    @Override
    public void validate(String pattern, Type... types) throws InvalidPatternException {

        if ((pattern == null) || pattern.trim().isEmpty()) {
            throw new InvalidPatternException("Pattern cannot be empty");
        }

        if (pattern.trim().equals(PLACEHOLDER)) {
            throw new InvalidPatternException("Pattern too generic");
        }

        int paramCount = types.length;
        int placeholderCount = countPlaceholders(pattern);
        if (paramCount != placeholderCount) {
            throw new InvalidPatternException(
                    String.format("Parameter count %d does not match the pattern: %d", paramCount, placeholderCount));
        }
    }

    private static int countPlaceholders(String pattern) {

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(pattern);

        int count = 0;
        int end = 0;
        while (matcher.find(end)) {

            if (isUnescaped(matcher.group())) {
                count++;
            }

            end = matcher.end();
        }

        return count;
    }

    private static boolean isUnescaped(String string) {

        // only exactly one backslash counts as escaping
        int count = 0;
        int i = 0;

        while (i < string.length() && string.charAt(i) == ESCAPE_CHAR) {
            i++;
            count++;
        }

        return count != 1;
    }

    @Override
    public int hashCode() {
        return Slf4jFormatter.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return "Slf4jFormatter {}";
    }
}
