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

import com.amdocs.textcrate.spi.MessagesProvider;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Implements a singleton for loading formatted messages according to message a repository specification defined by
 * a plain Java interface using special annotations.</p>
 *
 * <p>First, and attempt is made to use one of the instances of {@link MessagesProvider} available via
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">java.util.ServiceLoader</a>, if any.
 * If no custom implementation has been configured, a default best-effort implementation will be used.</p>
 *
 * <p><strong>Note:</strong> Caching of the message repositories is the responsibility of a client and/or custom factory
 * service implementation. We cannot predict if the application wants to allow reloading messages in runtime.
 * On the other hand, caching is easy by just holding a reference to a repository in a variable. Example:</p>
 *
 * <pre>
 *
 * public class Messages {
 *     public static ErrorMessages ERRORS = Messages.from(ErrorMessages.class);
 *     public static WarningMessages WARNINGS = Messages.from(WarningsMessages.class);
 * }
 *
 * </pre>
 *
 * @author evitaliy
 * @since 13 Oct 2016
 */
public class Messages {

    private static final ProxyMessagesProvider FALLBACK = new ProxyMessagesProvider();

    private Messages() { /* prevent instantiation */ }

    /**
     * Entry point for getting an instance that represents a message repository.
     *
     * @param clazz interface that defines messages with their codes, patterns, parameters and metadata
     * @param <T> concrete interface type
     * @return object that implements the message repository interface
     */
    public static <T> T from(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        ServiceLoader<MessagesProvider> loader = ServiceLoader.load(MessagesProvider.class);
        return loadFromProvider(clazz, loader);
    }

    /**
     * Entry point for getting an instance that represents a message repository using a custom class loader.
     *
     * @param clazz interface that defines messages with their codes, patterns, parameters and metadata
     * @param <T> concrete interface type
     * @param classLoader non-default class loader for looking up custom repository implementations
     * @return object that implements the message repository interface
     */
    public static <T> T from(Class<T> clazz, ClassLoader classLoader) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        ServiceLoader<MessagesProvider> loader = ServiceLoader.load(MessagesProvider.class, classLoader);
        return loadFromProvider(clazz, loader);
    }

    @SuppressWarnings({"squid:S3655", "ConstantConditions"}) //// isPresent() on FALLBACK must always succeed
    private static <T> T loadFromProvider(Class<T> clazz, ServiceLoader<MessagesProvider> loader) {

        for (MessagesProvider provider : loader) {

            Optional<T> messages = provider.getMessages(clazz);
            if (messages.isPresent()) {
                logLoading(clazz, provider);
                return messages.get();
            }
        }

        return FALLBACK.getMessages(clazz).get();
    }

    private static <T> void logLoading(Class<T> clazz, MessagesProvider provider) {

        Logger logger = LoggerFactory.getLogger(Messages.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Messages for {} are provided by {}", clazz.getName(), provider);
        }
    }
}
