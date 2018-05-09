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
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.amdocs.textcrate.api.Formatter;
import com.amdocs.textcrate.api.Message;
import com.amdocs.textcrate.api.Validator;
import com.amdocs.textcrate.api.annotations.CodeSpec;
import com.amdocs.textcrate.api.annotations.MessageFormatter;
import com.amdocs.textcrate.api.annotations.MessageProperty;
import com.amdocs.textcrate.api.annotations.MessageSpec;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Optional;
import org.testng.annotations.Test;

/**
 * Tests the behavior of the proxy-based provider.
 *
 * @author evitaliy
 * @since 18 Sep 2016
 */
public class ProxyMessagesProviderTest {


    private static final String IS_STILL_ALIVE = "{} is still alive!";
    private static final String PROP_ONE_KEY = "one";
    private static final String PROP_TWO_KEY = "two";
    private static final String PROP_ONE_VALUE = "1";
    private static final String PROP_TWO_VALUE = "2";

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            "[\\w\\.]+\\.ProxyMessagesProviderTest\\$ConcreteClassMessages is not an interface")
    public void proxyingMessagesFailsWhenInputIsConcreteClass() {
        new ProxyMessagesProvider().getMessages(ConcreteClassMessages.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            "[\\w\\.]+\\.ProxyMessagesProviderTest\\$AbstractClassMessages is not an interface")
    public void proxyingMessagesFailsWhenInputIsAbstractClass() {
        new ProxyMessagesProvider().getMessages(AbstractClassMessages.class);
    }

    @Test
    public void proxyingMessagesSucceedsWhenInputClassUnannotated() {
        final Optional<UnAnnotatedMessages> messages =
                new ProxyMessagesProvider().getMessages(UnAnnotatedMessages.class);
        assertTrue(messages.isPresent());
    }

    @Test
    public void messageIsStringWhenMethodReturnTypeIsCharSequence() {
        final Optional<ProxyMessagesProviderTest.TestMessages>
                messages = new ProxyMessagesProvider().getMessages(ProxyMessagesProviderTest.TestMessages.class);
        assertTrue(messages.isPresent());
        assertEquals(messages.get().stringReturnType(), "Hi");
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
                "com.amdocs.textcrate.ProxyMessagesProviderTest$TestMessages:missingAnnotation");
        assertEquals(message.getMessage(),
                "Unannotated message: com.amdocs.textcrate.ProxyMessagesProviderTest$TestMessages#"
                        + "missingAnnotation([argument])");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            ".*\\.api\\.Message.*")
    public void gettingMessageThrowsExceptionWhenReturnTypeNotStringOrMessage() {
        final Optional<TestMessages> messages = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        messages.get().incorrectReturnType();
    }

    @Test
    public void standardImplementationInvokedWhenToStringCalled() {
        final Optional<FormattedCodeMessages> messages =
                new ProxyMessagesProvider().getMessages(FormattedCodeMessages.class);
        assertTrue(messages.isPresent());
        assertTrue(messages.get().toString().startsWith("ProxyMessagesProvider.MessageRepositoryInvocationHandler("));
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
        assertEquals(message.getMessage(), "Pattern: '{} is still alive!'. Arguments: [This method]");
    }

    @Test
    public void messageReturnedEvenWhenFormatterCannotBeInstantiated() {
        final Optional<BadFormatterMessages> messages =
                new ProxyMessagesProvider().getMessages(BadFormatterMessages.class);
        assertTrue(messages.isPresent());
        final Message message = messages.get().canFormat();
        assertEquals(message.getPattern(), "Still there");
        assertEquals(message.getArguments(), new Object[0]);
        assertEquals(message.getMessage(), "Pattern: 'Still there'. Arguments: []");
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
        assertEquals(data.toString(),
                "2147483647 Unannotated message: com.amdocs.textcrate.ProxyMessagesProviderTest$TestMessages"
                        + "#missingAnnotation([arg])");
    }

    @Test
    public void propertiesRetainedEvenWhenUnannotatedMessage() {
        final Optional<MessagesWithProperties> messages =
                new ProxyMessagesProvider().getMessages(MessagesWithProperties.class);
        assertTrue(messages.isPresent());
        final Message data = messages.get().unAnnotated("arg");
        assertEquals(data.getProperty(PROP_ONE_KEY), PROP_ONE_VALUE);
        assertEquals(data.getProperty(PROP_TWO_KEY), PROP_TWO_VALUE);
    }

    @Test
    public void messageReturnedEvenWhenUnannotatedAndCharSequenceReturnType() {
        final Optional<TestMessages> messages =
                new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        final String data = messages.get().stringTypeWithoutAnnotation("argument");
        assertEquals(data, "Unannotated message: com.amdocs.textcrate.ProxyMessagesProviderTest$"
                                   + "TestMessages#stringTypeWithoutAnnotation([argument])");
    }

    @Test
    public void instancesEqualWhenFromSameInterface() {

        final Optional<TestMessages> optionalOne = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(optionalOne.isPresent());

        final Optional<TestMessages> optionalTwo = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(optionalTwo.isPresent());

        TestMessages instanceOne = optionalOne.get();
        TestMessages instanceTwo = optionalTwo.get();
        assertEquals(instanceOne, instanceTwo);
        assertEquals(instanceOne.hashCode(), instanceTwo.hashCode());
    }

    @Test
    public void instancesNotEqualWhenFromDifferentInterfaces() {

        final Optional<TestMessages> optionalOne = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(optionalOne.isPresent());

        final Optional<FormattedCodeMessages> optionalTwo =
                new ProxyMessagesProvider().getMessages(FormattedCodeMessages.class);

        assertTrue(optionalTwo.isPresent());
        assertNotEquals(optionalOne.get(), optionalTwo.get());
    }

    @Test
    public void meaningfulStringReturnedWhenToStringInvoked() {
        final Optional<TestMessages> messages = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        assertTrue(messages.get().toString().startsWith("ProxyMessagesProvider.MessageRepositoryInvocationHandler("));
    }

    @Test
    public void notEqualWhenNotProxy() {
        final Optional<UnAnnotatedMessages> messages =
                new ProxyMessagesProvider().getMessages(UnAnnotatedMessages.class);
        assertTrue(messages.isPresent());
        UnAnnotatedMessages proxy = messages.get();
        UnAnnotatedMessages concrete = new UnAnnotatedMessagesImpl();
        //noinspection SimplifiedTestNGAssertion
        assertFalse(proxy.equals(concrete));
    }

    @Test
    public void notEqualWhenFromDifferentInterfaces() {

        final Optional<TestMessages> one = new ProxyMessagesProvider().getMessages(TestMessages.class);
        final Optional<FormattedCodeMessages> two =
                new ProxyMessagesProvider().getMessages(FormattedCodeMessages.class);

        assertTrue(one.isPresent());
        assertTrue(two.isPresent());

        Object messagesOne = one.get();
        Object messagesTwo = two.get();

        //noinspection SimplifiedTestNGAssertion
        assertFalse(messagesTwo.equals(messagesOne));
    }

    @Test
    public void notEqualWhenDifferentProxyImplementations() {

        final Optional<TestMessages> messages = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());

        final ClassLoader classLoader = TestMessages.class.getProtectionDomain().getClassLoader();
        Object otherProxy = Proxy.newProxyInstance(classLoader, new Class<?>[] {TestMessages.class},
                (proxy, method, args) -> null);

        Object messagesProxy = messages.get();
        //noinspection SimplifiedTestNGAssertion
        assertFalse(messagesProxy.equals(otherProxy));
    }

    @Test
    public void notEqualWhenComparedToNull() {
        final Optional<TestMessages> messages = new ProxyMessagesProvider().getMessages(TestMessages.class);
        assertTrue(messages.isPresent());
        //noinspection SimplifiedTestNGAssertion,ObjectEqualsNull,ConstantConditions
        assertFalse(messages.get().equals(null));
    }

    private interface UnAnnotatedMessages { }

    private static class UnAnnotatedMessagesImpl implements UnAnnotatedMessages { }

    private interface TestMessages {

        String stringTypeWithoutAnnotation(String argument);

        Message missingAnnotation(String argument);

        @MessageSpec(id = 0, pattern = "Hi")
        CharSequence stringReturnType();

        @MessageSpec(id = 200, pattern = "Hi")
        void incorrectReturnType();
    }

    @CodeSpec(pattern = "[APP]:{}-code", offset = 2000)
    private interface FormattedCodeMessages {

        @MessageSpec(id = 112, pattern = "Hello, {}!")
        Message hello(String name);
    }

    @MessageProperty(name = PROP_ONE_KEY, value = PROP_ONE_VALUE)
    @MessageProperty(name = PROP_TWO_KEY, value = PROP_TWO_VALUE)
    private interface MessagesWithProperties {

        Message unAnnotated(String argument);
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
    static class FailingFormatter implements Formatter {

        @Override
        public String format(String pattern, Object... arguments) {
            throw new UnsupportedOperationException("format");
        }

        @Override
        public Optional<Validator> getValidator() {
            return Optional.empty();
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
        public Optional<Validator> getValidator() {
            return Optional.empty();
        }
    }
}