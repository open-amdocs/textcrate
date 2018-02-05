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

package com.amdocs.messageformatting.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marks interface methods that construct formatted messages. Such a method is expected to return an instance of
 * {@link com.amdocs.messageformatting.api.Message}. The annotation can be processed to generate a concrete
 * implementation of message formatting either during build, or in run time.</p>
 *
 * <p>For example, calling this method with <code>guest</code> as an argument</p>
 *
 * <pre>
 *     &#64;MessageSpec(id = 401, pattern = "User '{}' is not authorized to access the page")
 *     Message userNotAuthorized(String username);
 * </pre>
 *
 * <p>should by default result in:</p>
 *
 * <pre>
 *     &quot;User 'guest' is not authorized to access the page&quot;
 * </pre>
 *
 * @author evitaliy
 * @since 22 Aug 2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MessageSpec {

    /**
     * <p>Defines the formatting pattern of a message. The default pattern can be overridden in an external source if
     * needed, for example for localization and/or proof-reading.</p>
     *
     * <p>The pattern may include placeholders for message parameters. The chosen formatting can be specified
     * at the repository level using {@link MessageFormatter}.
     *
     * @return message pattern to be used if no other versions were found
     */
    String pattern();

    /**
     * The numeric ID of a message. All IDs must be unique within their message repository.
     *
     * @return a unique numeric ID
     */
    int id();
}
