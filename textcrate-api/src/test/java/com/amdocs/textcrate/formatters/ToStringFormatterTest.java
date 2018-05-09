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

package com.amdocs.textcrate.formatters;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import com.amdocs.textcrate.api.Validator;
import java.util.Optional;
import org.testng.annotations.Test;

/**
 * Test basic behavior of simple string formatting.
 *
 * @author evitaliy
 * @since 08 May 2018
 */
public class ToStringFormatterTest {

    @Test
    public void messageNotNullWhenArgumentsNulls() {
        assertNotNull(new ToStringFormatter().format(null, (Object[]) null));
    }

    @Test
    public void messageCorrectWhenArgumentsGiven() {
        assertNotNull(new ToStringFormatter().format("P", 1, "2"), "Pattern: 'P', Arguments: [1, 2]");
    }

    @Test
    public void doesNotValidate() {
        Optional<Validator> validator = new ToStringFormatter().getValidator();
        assertFalse(validator.isPresent());
    }
}