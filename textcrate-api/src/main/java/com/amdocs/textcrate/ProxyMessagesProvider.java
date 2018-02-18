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
import com.amdocs.textcrate.api.InvalidPatternException;
import com.amdocs.textcrate.api.Message;
import com.amdocs.textcrate.api.annotations.CodeSpec;
import com.amdocs.textcrate.api.annotations.MessageFormatter;
import com.amdocs.textcrate.api.annotations.MessageProperty;
import com.amdocs.textcrate.api.annotations.MessageSpec;
import com.amdocs.textcrate.spi.MessagesProvider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fallback implementation of {@link MessagesProvider} that, given an interface, analyzes its
 * annotations and constructs message formats on the fly. It is resilient, but not efficient because
 * of a heavy use of Java reflection.
 *
 * @author evitaliy
 * @since 18/09/2016.
 */
class ProxyMessagesProvider implements MessagesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyMessagesProvider.class);

    private static final Formatter DEFAULT_MESSAGE_FORMATTER =
            new ResilientFormatter(MessageFormatter.DEFAULT_FORMATTER);

    /**
     * Should be used when no message code pattern was specified. Otherwise, an "if" statement would be needed to check
     * the code formatter for <code>null</code> every time a message code is requested.
     */
    private static final Formatter DEFAULT_MSG_CODE_FORMATTER = new AsIsFormatter();

    /**
     * Return something meaningful when all other attempts failed.
     */
    private static final Formatter FALLBACK_FORMATTER = new ToStringFormatter();

    @Override
    public <T> Optional<T> getMessages(Class<T> clazz) {
        Object repo = Proxy.newProxyInstance(clazz.getProtectionDomain().getClassLoader(),
                new Class[]{clazz}, new MessageRepositoryInvocationHandler(clazz));
        return Optional.of(clazz.cast(repo));
    }

    private static class MessageRepositoryInvocationHandler implements InvocationHandler {

        private static final Object[] EMPTY_ARGS = new Object[0];

        private final String className;
        private final Formatter formatter;
        private final CodeBlueprint.Formatting codeFormatting;
        private final Map<String, String> properties;
        private final Map<Method, MessageBlueprint> messages = new ConcurrentHashMap<>();

        private <T> MessageRepositoryInvocationHandler(Class<T> clazz) {
            this.className = clazz.getName();
            this.formatter = initMessageFormatter(clazz);
            this.codeFormatting = initCodeFormatting(clazz, this.formatter);
            this.properties = initProperties(clazz);
        }

        private <T> Formatter initMessageFormatter(Class<T> clazz) {

            MessageFormatter formatAnnotation = clazz.getAnnotation(MessageFormatter.class);
            if (formatAnnotation == null) {
                LOGGER.debug("No format annotation on {}. Default will be used", clazz.getName());
                return DEFAULT_MESSAGE_FORMATTER;
            }

            Formatter requestedFormatter;

            try {
                requestedFormatter = formatAnnotation.type().newInstance();
            } catch (Exception e) {
                LOGGER.error("Formatter " + formatAnnotation.type() + " could not be instantiated", e);
                return FALLBACK_FORMATTER;
            }

            return new ResilientFormatter(requestedFormatter);
        }

        private <T> CodeBlueprint.Formatting initCodeFormatting(Class<T> clazz, Formatter formatter) {

            CodeSpec codeAnnotation = clazz.getAnnotation(CodeSpec.class);
            if (codeAnnotation == null) {
                LOGGER.debug("No message code annotation on {}. Default will be used", clazz.getName());
                return new CodeBlueprint.Formatting(0, "", DEFAULT_MSG_CODE_FORMATTER);
            } else {
                return new CodeBlueprint.Formatting(codeAnnotation.offset(), codeAnnotation.pattern(), formatter);
            }
        }

        private <T> Map<String, String> initProperties(Class<T> clazz) {

            MessageProperty[] propsAnnotation = clazz.getAnnotationsByType(MessageProperty.class);
            return propsAnnotation.length == 0 ? Collections.emptyMap() : Arrays.stream(propsAnnotation)
                    .collect(Collectors.toMap(MessageProperty::name, MessageProperty::value));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Object[] normalizedArgs = args == null ? EMPTY_ARGS : args;

            MessageSpec annotation = method.getAnnotation(MessageSpec.class);
            if (annotation == null) {
                LOGGER.debug("Method not annotated: {}. Trying to call anyway", method.getName());
                return invokeUnannotated(method, normalizedArgs, properties);
            }

            MessageBlueprint blueprint = this.messages.computeIfAbsent(method, key -> {
                BaseMessageBlueprint.Formatting formatting =
                        new BaseMessageBlueprint.Formatting(annotation.pattern(), formatter);
                CodeBlueprint codeBlueprint = new CodeBlueprint(annotation.id(), this.codeFormatting);
                return new BaseMessageBlueprint(codeBlueprint, formatting, properties);
            });

            return getMessage(method.getReturnType(), blueprint, normalizedArgs);
        }

        private Object invokeUnannotated(Method method, Object[] args, Map<String, String> properties)
                throws InvocationTargetException, IllegalAccessException {

            try {
                // May be a common method like toString(), hashCode(), etc.
                Method self = this.getClass().getMethod(method.getName(), method.getParameterTypes());
                return self.invoke(this, args);
            } catch (NoSuchMethodException e) {

                // If forgot to annotate
                MessageBlueprint blueprint = this.messages.computeIfAbsent(method, key ->
                        new UnannotatedMessageBlueprint(method, properties));
                return getMessage(method.getReturnType(), blueprint, args);
            }
        }

        private Object getMessage(Class returnType, MessageBlueprint blueprint, Object[] args) {

            if (Message.class.isAssignableFrom(returnType)) {
                return new BaseMessage(blueprint, args);
            }

            if (CharSequence.class.isAssignableFrom(returnType)) {
                // Make best effort to return something meaningful
                return blueprint.format(args);
            }

            // Can do nothing :(
            throw new IllegalArgumentException(String.format("Message method must return %s", Message.class.getName()));
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (o == null || !(o instanceof Proxy)) {
                return false;
            }

            // must be a proxy with the same type of invocation handler
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
            if (invocationHandler.getClass() != this.getClass()) {
                return false;
            }

            MessageRepositoryInvocationHandler that = (MessageRepositoryInvocationHandler) invocationHandler;
            return Objects.equals(className, that.className); // leave out the other fields
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getClass(), className); // leave out the other fields
        }

        @Override
        public String toString() {
            return "MessageRepositoryInvocationHandler {className=" + className + ", messageFormatter=" + formatter
                + ", codeFormatting=" + codeFormatting + ", properties=" + properties + '}';
        }
    }

    /**
     * Best-effort message construction for an unannotated method.
     */
    private static class UnannotatedMessageBlueprint implements MessageBlueprint {

        private static final String CODE = Integer.toString(Integer.MAX_VALUE);

        private final Method method;
        private final Map<String, String> properties;

        private UnannotatedMessageBlueprint(Method method, Map<String, String> properties) {
            this.method = method;
            this.properties = properties;
        }

        @Override
        public String format(Object[] arguments) {
            return String.format("Unannotated message: %s#%s(%s)",
                    method.getDeclaringClass().getName(), method.getName(), Arrays.toString(arguments));
        }

        @Override
        public String getPattern() {
            return method.getDeclaringClass().getName() + ":" + method.getName();
        }

        @Override
        public String getCode() {
            return CODE;
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

            UnannotatedMessageBlueprint that = (UnannotatedMessageBlueprint) o;
            return Objects.equals(method, that.method) && Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, properties);
        }

        @Override
        public String toString() {
            return "UnannotatedBlueprint {method=" + method.getName() + ", properties=" + properties + '}';
        }
    }

    /**
     * Makes best effort returning a meaningful message if the desired formatter
     * can't do it or fails unexpectedly.
     */
    private static class ResilientFormatter implements Formatter {

        private final Formatter delegate;

        ResilientFormatter(Formatter formatter) {
            this.delegate = formatter;
        }

        @Override
        public String format(String pattern, Object... arguments) {

            try {
                return delegate.format(pattern, arguments);
            } catch (Exception e) {
                return FALLBACK_FORMATTER.format(pattern, arguments);
            }
        }

        @Override
        public void validate(String pattern, Type... types) throws InvalidPatternException {

            try {
                delegate.validate(pattern, types);
            } catch (InvalidPatternException ipe) {
                throw ipe;
            } catch (Exception e) {
                FALLBACK_FORMATTER.validate(pattern, types);
            }
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ResilientFormatter that = (ResilientFormatter) o;
            return Objects.equals(delegate, that.delegate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(delegate);
        }

        @Override
        public String toString() {
            return "ResilientFormatter {delegate=" + delegate + '}';
        }
    }

    /**
     * Should be used when the formatting rules are unknown. In this case both the pattern and the arguments of
     * a message will be converted to <code>String</code> "AS IS", without the pattern being applied to the arguments.
     */
    private static class ToStringFormatter implements Formatter {

        @Override
        public String format(String pattern, Object... arguments) {
            return "Pattern: '" + pattern + "'. Arguments: " + Arrays.toString(arguments);
        }

        @Override
        public void validate(String pattern, Type... types) { /* accept everything */ }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && this.getClass().equals(obj.getClass());
        }

        @Override
        public String toString() {
            return "ToStringFormatter{}";
        }
    }

    /**
     * Returns the first argument "AS IS", without using any pattern.
     * Must be used only when there is at least one argument.
     */
    private static class AsIsFormatter implements Formatter {

        @Override
        public String format(String pattern, Object... arguments) {
            return String.valueOf(arguments[0]);
        }

        @Override
        public void validate(String pattern, Type... types) { /* do nothing */ }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && this.getClass().equals(obj.getClass());
        }

        @Override
        public String toString() {
            return "AsIsFormatter{}";
        }
    }
}
