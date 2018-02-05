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

package com.amdocs.messageformatting.formatters;

import com.amdocs.messageformatting.api.InvalidPatternException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author evitaliy
 * @since 16 Oct 17
 */
public class Slf4jFormatterTest {

    private static final String NO_MATCH = ".*not match.*";
    private static final String TOO_GENERIC = ".*generic.*";
    private static final String EMPTY_PATTERN = ".*empty.*";

    @Test
    public void outputEmptyStringWhenPatternEmpty() {
        assertEquals(new Slf4jFormatter().format("", "message"), "");
    }

    @Test
    public void outputArgumentWhenPatternHasPlaceholderOnly() {
        assertEquals(new Slf4jFormatter().format("{}", "message"), "message");
    }

    @Test
    public void outputUnescapedPlaceholderWhenEscapedInPattern() {
        assertEquals(new Slf4jFormatter().format("\\{}", "message"), "{}");
    }

    @Test
    public void outputEscapedArgumentWhenPlaceholderDoubleEscaped() {
        assertEquals(new Slf4jFormatter().format("\\\\{}", "message"), "\\message");
    }

    @Test(expectedExceptions = InvalidPatternException.class, expectedExceptionsMessageRegExp = EMPTY_PATTERN)
    public void validationThrowsExceptionWhenPatternEmptyString() throws InvalidPatternException {
        new Slf4jFormatter().validate("");
    }

    @Test(expectedExceptions = InvalidPatternException.class, expectedExceptionsMessageRegExp = EMPTY_PATTERN)
    public void validationThrowsExceptionWhenPatternOnlySpaces() throws InvalidPatternException {
        new Slf4jFormatter().validate("     ");
    }

    @Test(expectedExceptions = InvalidPatternException.class, expectedExceptionsMessageRegExp = TOO_GENERIC)
    public void validationThrowsExceptionWhenPatternPlaceholderOnly() throws InvalidPatternException {
        new Slf4jFormatter().validate("{}");
    }

    @Test(expectedExceptions = InvalidPatternException.class, expectedExceptionsMessageRegExp = TOO_GENERIC)
    public void validationThrowsExceptionWhenPatternHasOnlyPlaceholderAndSpaces() throws InvalidPatternException {
        new Slf4jFormatter().validate("   {}   ");
    }

    @Test(expectedExceptions = InvalidPatternException.class, expectedExceptionsMessageRegExp = NO_MATCH)
    public void validationThrowsExceptionWhenPatternRequiresParameterButNoneGiven() throws InvalidPatternException {
        new Slf4jFormatter().validate("Message: {}");
    }

    @Test(expectedExceptions = InvalidPatternException.class, expectedExceptionsMessageRegExp = NO_MATCH)
    public void validationThrowsExceptionWhenPatternWithoutParametersButOneGiven() throws InvalidPatternException {
        new Slf4jFormatter().validate("Message without placeholders", String.class);
    }

    @Test(expectedExceptions = InvalidPatternException.class, expectedExceptionsMessageRegExp = NO_MATCH)
    public void validationThrowsExceptionWhenPatternRequiresOneParametersButTwoGiven() throws InvalidPatternException {
        new Slf4jFormatter().validate("Message argument {}", Integer.TYPE, String.class);
    }

    @Test
    public void validationSucceedsWhenPlaceholderEscapedAndNoParametersGiven() throws InvalidPatternException {
        new Slf4jFormatter().validate("This is not a placeholder: \\{}");
    }

    @Test
    public void validationSucceedsWhenPlaceholderDoubleEscapedAndOneParametersGiven() throws InvalidPatternException {
        new Slf4jFormatter().validate("This is a valid placeholder: \\\\{}", String.class);
    }

    @Test
    public void validationSucceedsWhenTwoPlaceholdersAndTwoParametersGiven() throws InvalidPatternException {
        new Slf4jFormatter().validate("Message arguments {} and {}, a total of two", Integer.TYPE, String.class);
    }
}