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

package com.amdocs.textcrate.api;

import java.util.Locale;

/**
 * <p>Represents a formatted message.</p>
 *
 * <p><b>Example usages</b></p>
 *
 * <p>Using a message &quot;as is&quot;, such as for throwing an exception:</p>
 *
 * <pre>
 *     Message authzError = repository.userNotAuthorized("guest");
 *     throw new IllegalArgumentException(authzError.getMessage());
 * </pre>
 *
 * <p>Deferring message construction for logging
 * (see <a href="http://slf4j.org/faq.html#logging_performance">What is the fastest way of (not) logging?</a>):</p>
 *
 * <pre>
 *     Message authzError = repository.userNotAuthorized("guest");
 *     LOGGER.error(authzError.getPattern(), authzError.getArguments());
 * </pre>
 *
 * @author evitaliy
 * @since 22 Aug 2017
 */
public interface Message {

    /**
     * Formatting pattern, according to which the message is constructed.
     *
     * @return parametrized message pattern
     */
    String getPattern();

    /**
     * Formatting pattern in a given {@link Locale}, according to which the message is constructed.
     *
     * @param locale desired locale; it is not recommended to send <code>null</code> here, although the exact behavior
     *               is left up to an implementation
     * @return parametrized localized message pattern
     */
    String getPattern(Locale locale);

    /**
     * Full text of a message, build using the message's formatting pattern ({@link #getPattern()}) and arguments
     * ({@link #getArguments()}).
     *
     * @return full text of the message
     */
    String getMessage();

    /**
     * Localized text of a message, build using the message's formatting pattern ({@link #getPattern(Locale)}) and
     * arguments (see {@link #getArguments()}).
     *
     * @param locale desired locale; it is not recommended to send <code>null</code> here, although the exact behavior
     *               is left up to an implementation
     * @return full localized text of the message
     */
    String getMessage(Locale locale);

    /**
     * Arguments used to construct this message. Normally, such arguments will be inserted into the placeholders
     * of a parametrized message pattern ({@link #getPattern()} and {@link #getPattern(Locale)}).
     *
     * @return message arguments
     */
    Object[] getArguments();

    /**
     * Unique code that represents this message in a message repository.
     *
     * @return unique message code
     */
    String getCode();

    /**
     * A custom property of the message.
     *
     * @param name property name (key), cannot be <code>null</code>
     * @return value that corresponds to the <i>name</i> or <code>null</code> if undefined
     */
    String getProperty(String name);
}
