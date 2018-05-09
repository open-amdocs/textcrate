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

import com.amdocs.textcrate.api.Message;
import com.amdocs.textcrate.api.annotations.MessageSpec;
import com.amdocs.textcrate.spi.MessagesProvider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fallback implementation of {@link MessagesProvider} that, given an interface, analyzes its
 * annotations and constructs message formats on the fly. It is resilient, but not efficient because
 * of a heavy use of Java reflection.
 *
 * @author evitaliy
 * @since 18 Sep 2016
 */
class ProxyMessagesProvider implements MessagesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyMessagesProvider.class);

    @Override
    public <T> Optional<T> getMessages(Class<T> clazz) {
        Object repo = Proxy.newProxyInstance(clazz.getProtectionDomain().getClassLoader(),
                new Class<?>[]{clazz}, new MessageRepositoryInvocationHandler(clazz));
        return Optional.of(clazz.cast(repo));
    }

    private static class MessageRepositoryInvocationHandler implements InvocationHandler {

        private static final Object[] EMPTY_ARGS = new Object[0];

        private final Class<?> originalType;
        private final AnnotatedMessageBlueprintFactory blueprintFactory;
        private final Map<Method, MessageBlueprint> blueprintCache;

        private <T> MessageRepositoryInvocationHandler(Class<T> clazz) {
            this.originalType = clazz;
            this.blueprintFactory = new AnnotatedMessageBlueprintFactory(clazz);
            this.blueprintCache = new ConcurrentHashMap<>();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Object[] normalizedArgs = args == null ? EMPTY_ARGS : args;

            MessageSpec annotation = method.getAnnotation(MessageSpec.class);
            if (annotation == null) {
                LOGGER.debug("Method not annotated: {}. Trying to call anyway", method.getName());
                return invokeUnannotated(method, normalizedArgs);
            }

            return createAnnotatedMessage(method, normalizedArgs, annotation);
        }

        private Object invokeUnannotated(Method method, Object[] args)
                throws InvocationTargetException, IllegalAccessException {

            try {
                return callStandardObjectMethod(method, args);
            } catch (NoSuchMethodException e) {
                return createUnannotatedMessage(method, args);
            }
        }

        private Object callStandardObjectMethod(Method method, Object[] args)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method self = this.getClass().getMethod(method.getName(), method.getParameterTypes());
            return self.invoke(this, args);
        }

        private Object createUnannotatedMessage(Method method, Object[] args) {
            MessageBlueprint blueprint = blueprintCache.computeIfAbsent(method,
                    key -> new UnannotatedMessageBlueprint(method, blueprintFactory.getProperties()));
            return createMessage(method.getReturnType(), blueprint, args);
        }

        private Object createAnnotatedMessage(Method method, Object[] normalizedArgs, MessageSpec annotation) {
            MessageBlueprint blueprint = blueprintCache.computeIfAbsent(method,
                    key -> blueprintFactory.createBlueprint(annotation));
            return createMessage(method.getReturnType(), blueprint, normalizedArgs);
        }

        private Object createMessage(Class<?> returnType, MessageBlueprint blueprint, Object[] args) {

            if (Message.class.isAssignableFrom(returnType)) {
                return new BaseMessage(blueprint, args);
            }

            if (CharSequence.class.isAssignableFrom(returnType)) {
                // Make best effort to return something meaningful
                return blueprint.format(args);
            }

            throw new IllegalArgumentException(String.format("Message method must return %s", Message.class.getName()));
        }

        @Override
        public boolean equals(Object o) {

            if (!(o instanceof Proxy)) {
                return false;
            }

            // must be a proxy with the same type of invocation handler
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
            if (invocationHandler.getClass() != this.getClass()) {
                return false;
            }

            MessageRepositoryInvocationHandler that = (MessageRepositoryInvocationHandler) invocationHandler;
            return Objects.equals(originalType, that.originalType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getClass(), originalType);
        }

        public String toString() {
            return "ProxyMessagesProvider.MessageRepositoryInvocationHandler("
                           + "originalType=" + this.originalType.getName()
                           + ", blueprintFactory=" + this.blueprintFactory + ")";
        }
    }
}
