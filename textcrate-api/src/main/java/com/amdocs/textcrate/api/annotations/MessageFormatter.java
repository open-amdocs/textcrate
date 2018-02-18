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

package com.amdocs.textcrate.api.annotations;

import com.amdocs.textcrate.api.Formatter;
import com.amdocs.textcrate.formatters.Slf4jFormatter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optionally, allows using a custom formatter with a message repository. If no custom formatter has been specified,
 * the formatter defined in {@link #DEFAULT_FORMATTER} should be used.
 *
 * @author evitaliy
 * @since 01 Nov 17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MessageFormatter {

    /**
     * This formatter should be used when a custom one is not specified using the annotation.
     */
    Formatter DEFAULT_FORMATTER = new Slf4jFormatter();

    /**
     * Defines a formatter for message patterns.
     *
     * @return class of a formatter that will be used to substitute the placeholders of a pattern with actual values
     */
    Class<? extends com.amdocs.textcrate.api.Formatter> type() default Slf4jFormatter.class;
}
