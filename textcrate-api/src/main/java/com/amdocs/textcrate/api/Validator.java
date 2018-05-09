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

import java.lang.reflect.Type;

/**
 * <p>Validates if a list of parameter types matches a formatting pattern. Can be useful to create
 * automatic tools to ensure correct message structure, such as validators and code generators.</p>
 *
 * <p>Can also enforce non-empty and non-generic patterns (i.e. Message: &lt;placeholder&gt;).</p>
 *
 * @author evitaliy
 * @since 08 May 2018
 */
public interface Validator {

    /**
     * Validates a pattern against formatting rules. The rules must match the {@link Formatter} this validator will be
     * used with.
     *
     * @param pattern formatting pattern
     * @param types list of parameter types
     * @throws InvalidPatternException if the format is incorrect or the parameter types do not match the pattern
     */
    void validate(String pattern, Type... types) throws InvalidPatternException;
}
