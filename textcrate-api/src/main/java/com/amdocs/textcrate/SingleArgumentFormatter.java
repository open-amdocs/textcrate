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
import com.amdocs.textcrate.api.Validator;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Returns the first argument "AS IS", without using any pattern.
 * Must be used only when there is at least one argument.
 *
 * @author evitaliy
 * @since 8 May 2018
 */
@EqualsAndHashCode
@ToString
class SingleArgumentFormatter implements Formatter {

    @Override
    public String format(String pattern, Object... arguments) {
        return String.valueOf(arguments[0]);
    }

    @Override
    public Optional<Validator> getValidator() {
        return Optional.empty();
    }
}
