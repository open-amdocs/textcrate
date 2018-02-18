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

package com.amdocs.textcrate.spi;

import com.amdocs.textcrate.api.annotations.CodeSpec;
import com.amdocs.textcrate.api.annotations.MessageSpec;
import java.util.Optional;

/**
 * <p>Implements a service that retrieves a repository specification defined by a Java interface annotated with
 *  {@link MessageSpec} and {@link CodeSpec}, and returns a concrete implementation &mdash; if available.</p>
 *
 * @author evitaliy
 * @since 18 Sep 2016
 */
public interface MessagesProvider {

    /**
     * <a>Returns a concrete implementation of a message repository specified by an interface.</p>
     *
     * @param clazz an interface that defines messages
     * @param <T> repository specification
     *
     * @return optionally, a concrete implementation of the interface given as the argument
     */
    <T> Optional<T> getMessages(Class<T> clazz);
}
