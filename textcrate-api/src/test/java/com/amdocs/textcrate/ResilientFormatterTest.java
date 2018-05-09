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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import com.amdocs.textcrate.api.Formatter;
import com.amdocs.textcrate.api.InvalidPatternException;
import com.amdocs.textcrate.api.Validator;
import com.amdocs.textcrate.formatters.Slf4jFormatter;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;
import org.testng.annotations.Test;

/**
 * Test fallback functionality.
 *
 * @author evitaliy
 * @since 08 May 2018
 */
public class ResilientFormatterTest {

    private static final String FALLBACK_OUTPUT = UUID.randomUUID().toString();

    @Test
    public void formattingDelegatedWhenInputCorrect() {

        final String pattern = "Hello, {}!";
        final String arg = "world";

        Slf4jFormatter delegate = new Slf4jFormatter();
        String expected = delegate.format(pattern, arg);

        SpyFormatter spy = new SpyFormatter();
        assertEquals(new ResilientFormatter(delegate, spy).format(pattern, arg), expected);
        assertFalse(spy.formatted);
    }

    @Test
    public void validationDelegatedWhenInputCorrect() throws InvalidPatternException {
        SpyFormatter spy = new SpyFormatter();
        new ResilientFormatter(new Slf4jFormatter(), spy).validate("No parameters");
        assertFalse(spy.validated);
    }

    @Test
    public void formattingFallsBackWhenInputIncorrect() {
        SpyFormatter spy = new SpyFormatter();
        assertEquals(new ResilientFormatter(new BrokenFormatter(), spy).format(null), FALLBACK_OUTPUT);
        assertTrue(spy.formatted);
    }

    @Test
    public void validationSkippedWhenDelegateDoesNotHaveValidator() throws InvalidPatternException {
        SpyFormatter spy = new SpyFormatter();
        new ResilientFormatter(new NonValidatingFormatter(), spy).validate(null);
        assertFalse(spy.validated);
    }

    @Test
    public void validationFallsBackWhenCorrectOneFails() throws InvalidPatternException {
        SpyFormatter spy = new SpyFormatter();
        new ResilientFormatter(new BrokenFormatter(), spy).validate(null);
        assertTrue(spy.validated);
    }

    @Test
    public void returnsResilientValidator() {
        ResilientFormatter formatter = new ResilientFormatter(null, null);
        Optional<Validator> validator = formatter.getValidator();
        assertTrue(validator.isPresent());
        assertSame(formatter, validator.get());
    }

    @Test(expectedExceptions = InvalidPatternException.class)
    public void doesNotFallsBackWhenExpectedError() throws InvalidPatternException {

        Slf4jFormatter delegate = new Slf4jFormatter();

        SpyFormatter spy = new SpyFormatter();

        try {
            new ResilientFormatter(delegate, spy).validate("One parameter: {}");
        } finally {
            assertFalse(spy.validated);
        }
    }

    private static class BrokenFormatter implements Formatter, Validator {

        @Override
        public String format(String pattern, Object... arguments) {
            throw new IllegalStateException();
        }

        @Override
        public Optional<Validator> getValidator() {
            return Optional.of(this);
        }

        @Override
        public void validate(String pattern, Type... types) {
            throw new IllegalStateException();
        }
    }

    private static class SpyFormatter implements Formatter, Validator {

        boolean formatted = false;
        boolean validated = false;

        @Override
        public String format(String pattern, Object... arguments) {
            formatted = true;
            return FALLBACK_OUTPUT;
        }

        @Override
        public Optional<Validator> getValidator() {
            return Optional.of(this);
        }

        @Override
        public void validate(String pattern, Type... types) {
            validated = true;
        }
    }

    private static class NonValidatingFormatter implements Formatter {

        @Override
        public String format(String pattern, Object... arguments) {
            return null;
        }

        @Override
        public Optional<Validator> getValidator() {
            return Optional.empty();
        }
    }
}