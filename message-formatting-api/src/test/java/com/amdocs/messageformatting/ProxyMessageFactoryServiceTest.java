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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.amdocs.messageformatting.api.Formatter;
import com.amdocs.messageformatting.api.Message;
import com.amdocs.messageformatting.api.annotations.CodeSpec;
import com.amdocs.messageformatting.api.annotations.MessageFormatter;
import com.amdocs.messageformatting.api.annotations.MessageProperty;
import com.amdocs.messageformatting.api.annotations.MessageSpec;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import org.testng.annotations.Test;

/**
 * @author evitaliy
 * @since 18/09/2016.
 */
public class ProxyMessageFactoryServiceTest {

    private static final String EXPECTED_MESSAGE_CODE = "[APP]:2112-code";
    private static final String IS_STILL_ALIVE = "{} is still alive!";
    private static final String PROP_ONE_KEY = "one";
    private static final String PROP_TWO_KEY = "two";
    private static final String PROP_ONE_VALUE = "1";
    private static final String PROP_TWO_VALUE = "2";

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            "[\\w\\.]+\\.ProxyMessageFactoryServiceTest\\$ConcreteClassMessages is not an interface")
    public void proxyingMessagesFailsWhenInputIsConcreteClass() {
        new ProxyMessagesProvider().getMessages(ConcreteClassMessages.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            "[\\w\\.]+\\.ProxyMessageFactoryServiceTest\\$AbstractClassMessages is not an interface")
    public void proxyingMessagesFailsWhenInputIsAbstractClass() {
        new ProxyMessagesProvider().getMessages(AbstractClassMessages.class);
    }

    @Test
    public void proxyingMessagesSucceedsWhenInputClassUnannotated() {
        final Optional<UnannotatedClass> messages = new ProxyMessagesProvider().getMessages(UnannotatedClass.class);
        assertTrue(messages.isPresent());
    }

    @Test
    public void fallbackMessageReturnedWhenMethodUnannotated() {

        final Optional<TestMessages> messages = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());

        final String argument = "argument";
        Message message = messages.get().missingAnnotation(argument);
        assertEquals(message.getArguments(), new String[]{ argument });
        assertEquals(message.getCode(), Integer.toString(Integer.MAX_VALUE));

        assertEquals(message.getPattern(),
                "com.amdocs.messageformatting.ProxyMessageFactoryServiceTest$TestMessages:missingAnnotation");
        assertEquals(message.getMessage(),
                "Unannotated message: com.amdocs.messageformatting.ProxyMessageFactoryServiceTest$TestMessages#" +
                        "missingAnnotation([argument])");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            ".*\\.api\\.Message.*")
    public void gettingMessageThrowsExceptionWhenReturnTypeNotStringOrMessage() {
        final Optional<TestMessages> messages = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        messages.get().incorrectReturnType();
    }

    @Test
    public void messageIsStringWhenMethodReturnTypeIsCharSequence() {
        final Optional<TestMessages> messages = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        assertEquals(messages.get().stringReturnType(), "Hi");
    }

    @Test
    public void codeIsZeroBasedWhenNoCodeAnnotationOnClass() {
        final Optional<TestMessages> messages = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().hundredCode();
        assertNotNull(data);
        assertEquals(data.getCode(), "100");
    }

    @Test
    public void offsetAddedWhenCodeAnnotationHasOffset() {
        final Optional<OffsetMessages> messages = new ProxyMessagesProvider().getMessages(OffsetMessages.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().coded();
        assertNotNull(data);
        assertEquals(data.getCode(), "1013");
    }

    @Test
    public void codePatternAppliedWhenCodeAnnotationHasPattern() {
        final Optional<FormattedCodeMessages> messages =
                new ProxyMessagesProvider().getMessages(FormattedCodeMessages.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().coded();
        assertNotNull(data);
        assertEquals(data.getCode(), "[APP]:2056-code");
    }

    @Test
    public void correctMessageReturnedWhenPatternHasOneParameter() {
        final Optional<FormattedCodeMessages> messages =
                new ProxyMessagesProvider().getMessages(FormattedCodeMessages.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().hello("world");
        assertNotNull(data);
        assertEquals(data.getPattern(), "Hello, {}!");
        assertEquals(data.getArguments(), new Object[] { "world" });
        assertEquals(data.getMessage(), "Hello, world!");
        assertEquals(data.getCode(), EXPECTED_MESSAGE_CODE);
    }

    @Test
    public void standardImplementationInvokedWhenToStringCalled() {
        final Optional<FormattedCodeMessages> messages =
                new ProxyMessagesProvider().getMessages(FormattedCodeMessages.class);
        assertTrue(messages.isPresent());
        assertEquals(messages.get().toString(),
                "MessageRepositoryInvocationHandler {className=" +
                        "com.amdocs.messageformatting.ProxyMessageFactoryServiceTest$FormattedCodeMessages, " +
                        "messageFormatter=ResilientFormatter {delegate=Slf4jFormatter {}}, " +
                        "codeFormatting=Formatting {offset=2000, pattern='[APP]:{}-code, " +
                        "formatter=ResilientFormatter {delegate=Slf4jFormatter {}}}, properties={}}");
    }

    @Test
    public void customFormatAppliedToCodeWhenCustomFormatterSpecified() {
        final Optional<CustomFormattedMessages> messages =
                new ProxyMessagesProvider().getMessages(CustomFormattedMessages.class);
        assertTrue(messages.isPresent());
        assertEquals(messages.get().test("dumb", 77).getMessage(), "MESSAGE/[dumb, 77]");
    }

    @Test
    public void customFormatAppliedToMessageWhenCustomFormatterSpecified() {
        final Optional<CustomFormattedMessages> messages =
                new ProxyMessagesProvider().getMessages(CustomFormattedMessages.class);
        assertTrue(messages.isPresent());
        assertEquals(messages.get().test("s", 1).getCode(), "CODE/[122]");
    }

    @Test
    public void nullReturnedWhenNoPropertyAnnotation() {
        final Optional<CustomFormattedMessages> messages =
                new ProxyMessagesProvider().getMessages(CustomFormattedMessages.class);
        assertTrue(messages.isPresent());
        assertNull(messages.get().test("a", 33).getProperty(PROP_ONE_KEY));
    }

    @Test
    public void propertiesReturnedWhenMultiplePropertyAnnotations() {
        final Optional<MessagesWithProperties> messages =
                new ProxyMessagesProvider().getMessages(MessagesWithProperties.class);
        assertTrue(messages.isPresent());
        final Message message = messages.get().test();
        assertEquals(message.getProperty(PROP_ONE_KEY), PROP_ONE_VALUE);
        assertEquals(message.getProperty(PROP_TWO_KEY), PROP_TWO_VALUE);
    }

    @Test
    public void messageReturnedEvenWhenFormatterThrowsException() {
        final Optional<ResilientMessages> messages =
                new ProxyMessagesProvider().getMessages(ResilientMessages.class);
        assertTrue(messages.isPresent());
        final String argument = "This method";
        final Message message = messages.get().isAlive(argument);
        assertEquals(message.getPattern(), IS_STILL_ALIVE);
        assertEquals(message.getArguments(), new String[] { argument });
        assertEquals(message.getMessage(), "Message: '{} is still alive!'. Arguments: [This method]");
    }

    @Test
    public void messageReturnedEvenWhenFormatterCannotBeInstantiated() {
        final Optional<BadFormatterMessages> messages =
                new ProxyMessagesProvider().getMessages(BadFormatterMessages.class);
        assertTrue(messages.isPresent());
        final Message message = messages.get().canFormat();
        assertEquals(message.getPattern(), "Still there");
        assertEquals(message.getArguments(), new Object[0]);
        assertEquals(message.getMessage(), "Message: 'Still there'. Arguments: []");
    }

    @Test
    public void sameOutputWhenLocalePassed() {
        final Optional<FormattedCodeMessages> messages =
                new ProxyMessagesProvider().getMessages(FormattedCodeMessages.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().hello("France");
        assertEquals(data.getPattern(), data.getPattern(Locale.FRENCH));
        assertEquals(data.getMessage(), data.getMessage(Locale.FRENCH));
    }

    @Test
    public void sameOutputWhenLocalePassedToUnannotatedMessage() {
        final Optional<TestMessages> messages =
                new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().missingAnnotation("Some text");
        assertEquals(data.getPattern(), data.getPattern(Locale.JAPAN));
        assertEquals(data.getMessage(), data.getMessage(Locale.JAPAN));
    }

    @Test
    public void toStringReturnsMeaningfulTextWhenUnannotatedMessage() {
        final Optional<TestMessages> messages =
                new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().missingAnnotation("arg");
        assertEquals(data.toString(), "BaseMessage {arguments=[arg], blueprint=UnannotatedBlueprint " +
                "{method=missingAnnotation, properties={}}}");
    }

    @Test
    public void propertiesRetainedEvenWhenUnannotatedMessage() {
        final Optional<MessagesWithProperties> messages =
                new ProxyMessagesProvider().getMessages(MessagesWithProperties.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().unAnnanoted("arg");
        assertEquals(data.getProperty(PROP_ONE_KEY), PROP_ONE_VALUE);
        assertEquals(data.getProperty(PROP_TWO_KEY), PROP_TWO_VALUE);
    }

    @Test
    public void messageReturnedEvenWhenUnannotatedAndCharSequenceReturnType() {
        final Optional<TestMessages> messages =
                new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        final String data = messages.get().stringTypeWithoutAnnotation("argument");
        assertEquals(data, "Unannotated message: com.amdocs.messageformatting.ProxyMessageFactoryServiceTest$" +
                "TestMessages#stringTypeWithoutAnnotation([argument])");
    }


    private interface UnannotatedClass { }

    private interface TestMessages {

        String stringTypeWithoutAnnotation(String argument);

        Message missingAnnotation(String argument);

        @MessageSpec(id = 0, pattern = "Hi")
        CharSequence stringReturnType();

        @MessageSpec(id = 100, pattern = "Hi")
        Message hundredCode();

        @MessageSpec(id = 200, pattern = "Hi")
        void incorrectReturnType();
    }

    @CodeSpec(pattern = "{}", offset = 1000)
    private interface OffsetMessages {

        @MessageSpec(id = 13, pattern = "Hi")
        Message coded();
    }

    @CodeSpec(pattern = "[APP]:{}-code", offset = 2000)
    private interface FormattedCodeMessages {

        @MessageSpec(id = 56, pattern = "Hi")
        Message coded();

        @MessageSpec(id = 112, pattern = "Hello, {}!")
        Message hello(String name);
    }

    @MessageFormatter(type = MockFormatter.class)
    @CodeSpec(pattern = "CODE")
    private interface CustomFormattedMessages {

        @MessageSpec(id = 122, pattern = "MESSAGE")
        Message test(String s, int i);
    }

    @MessageProperty(name = PROP_ONE_KEY, value = PROP_ONE_VALUE)
    @MessageProperty(name = PROP_TWO_KEY, value = PROP_TWO_VALUE)
    private interface MessagesWithProperties {

        @MessageSpec(id = 0, pattern = "{}")
        Message test();

        Message unAnnanoted(String argument);
    }

    @MessageFormatter(type = FailingFormatter.class)
    private interface ResilientMessages {

        @MessageSpec(id = 1, pattern = IS_STILL_ALIVE)
        Message isAlive(String who);
    }

    @MessageFormatter(type = NonInstantiableFormatter.class)
    private interface BadFormatterMessages {

        @MessageSpec(id = 100, pattern = "Still there")
        Message canFormat();
    }

    private static class ConcreteClassMessages { }

    private abstract static class AbstractClassMessages { }

    @SuppressWarnings("WeakerAccess") // class accessed using reflection
    static class MockFormatter implements Formatter {

        @Override
        public String format(String pattern, Object... arguments) {
            return pattern + "/" + Arrays.toString(arguments);
        }

        @Override
        public void validate(String pattern, Type... types) {
            throw new UnsupportedOperationException("validate");
        }
    }

    @SuppressWarnings("WeakerAccess") // class accessed using reflection
    static class FailingFormatter implements Formatter {

        @Override
        public String format(String pattern, Object... arguments) {
            throw new UnsupportedOperationException("format");
        }

        @Override
        public void validate(String pattern, Type... types) {
            throw new UnsupportedOperationException("validate");
        }
    }

    static class NonInstantiableFormatter implements Formatter {

        NonInstantiableFormatter() {
            throw new UnsupportedOperationException("constructor");
        }

        @Override
        public String format(String pattern, Object... arguments) {
            throw new UnsupportedOperationException("format");
        }

        @Override
        public void validate(String pattern, Type... types) {
            throw new UnsupportedOperationException("validate");
        }
    }
}