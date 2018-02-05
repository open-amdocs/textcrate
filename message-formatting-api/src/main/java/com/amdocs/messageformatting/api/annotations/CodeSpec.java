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
 * Optionally, specifies a custom format for message codes of a repository.
 *
 * @author evitaliy
 * @since 22 Aug 2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CodeSpec {

    /**
     * By default all message codes have zero offset.
     *
     * @see #offset()
     */
    int DEFAULT_CODE_OFFSET = 0;

    /**
     * <p>Optionally, the message in a repository may have codes with a common prefix, or suffix, or both. This
     * attribute enables to define a pattern for message codes. The unique numeric ID of a message will be inserted
     * into the pattern to construct a meaningful text code. The pattern must be in a format supported by
     * {@link MessageFormatter#type()}.</p>
     *
     * <p>For example, a message with the numeric ID</p>
     *
     * <pre>
     *     401
     * </pre>
     *
     * <p>in a repository with</p>
     *
     * <pre>
     *     &#64;CodeSpec(pattern = &quot;HTTP-{}&quot;)
     *     public interface HttpErrorCodes {
     *         ...
     *     }
     * </pre>
     *
     * <p>will have the actual code</p>
     *
     * <pre>
     *      &quot;HTTP-401&quot;
     * </pre>
     *
     * @return message code pattern
     */
    String pattern();

    /**
     * <p>Optionally, all message codes can start at some initial offset. For example, if the offset equals
     * <i>100</i>, then message IDs <i>1, 2, 3,</i> and so on will produce message codes <i>101, 102, 103,</i> etc.</p>
     *
     * <p>If the requirement is for all message code to have the same prefix, use {@link #pattern()} &mdash;
     * <i>&quot;HTTP40{}&quot;</i>, for instance, will result in <i>HTTP401, HTTP402, HTTP403,</i> and so on.</p>
     *
     * <p>By default, the offset equals {@link #DEFAULT_CODE_OFFSET}.</p>
     *
     * @return numeric code offset
     */
    int offset() default DEFAULT_CODE_OFFSET;
}
