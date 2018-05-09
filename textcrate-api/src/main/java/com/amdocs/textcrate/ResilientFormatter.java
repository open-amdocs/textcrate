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
import com.amdocs.textcrate.api.Validator;
import java.lang.reflect.Type;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes best effort returning a meaningful message if the desired formatter
 * can't do it or fails unexpectedly.
 *
 * @author evitaliy
 * @since 8 May 2018
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
class ResilientFormatter implements Formatter, Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResilientFormatter.class);

    private final Formatter delegate;
    private final Formatter fallback;

    @Override
    public String format(String pattern, Object... arguments) {

        try {
            return delegate.format(pattern, arguments);
        } catch (Exception e) {
            LOGGER.error("Failed to format message using {} with pattern '{}' and arguments '{}'. Falling back to {}.",
                    delegate, pattern, arguments, fallback, e);
            return fallback.format(pattern, arguments);
        }
    }

    @Override
    public Optional<Validator> getValidator() {
        return Optional.of(this);
    }

    @Override
    public void validate(String pattern, Type... types) throws InvalidPatternException {

        try {
            validate(delegate, pattern, types);
        } catch (InvalidPatternException ipe) {
            throw ipe;
        } catch (Exception e) {
            LOGGER.error("Failed to validate pattern '{}' with types '{}' using {}. Falling back to {}.",
                    pattern, types, delegate, fallback, e);
            validate(fallback, pattern, types);
        }
    }

    private void validate(Formatter formatter, String pattern, Type... types) throws InvalidPatternException {

        Optional<Validator> originalValidator = formatter.getValidator();
        if (originalValidator.isPresent()) {
            originalValidator.get().validate(pattern, types);
        }
    }
}
