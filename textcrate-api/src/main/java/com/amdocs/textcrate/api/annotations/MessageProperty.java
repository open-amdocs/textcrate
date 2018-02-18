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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Allows to configure custom repository-wide properties that will be copied to each message in this repository.</p>
 *
 * <p>For example, a user wants to have an application-specific message type or category as follows.</p>
 *
 * <ul>
 *     <li>100 - success</li>
 *     <li>200 - validation errors</li>
 *     <li>300 - unexpected errors</li>
 *     <li>and so on</li>
 * </ul>
 *
 * <p>In this case, the repository can be annotated with</p>
 *
 * <pre>
 *     &#64;MessageProperty(name="type", value="200")
 *     &#64;MessageProperty(name="typeDescription", value="Validation Errors")
 *     public interface ValidationErrors {
 *
 *     }
 * </pre>
 *
 * @author evitaliy
 * @since 23 Jan 18
 */
@Repeatable(MessageProperty.Properties.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MessageProperty {

    /**
     * Property name.
     *
     * @return name/key of the property
     */
    String name();

    /**
     * Property value.
     *
     * @return value of the property
     */
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Properties {
        MessageProperty[] value();
    }
}

