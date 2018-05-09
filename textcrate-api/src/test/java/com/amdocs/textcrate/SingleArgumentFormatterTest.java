/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.amdocs.textcrate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

/**
 * Basic testing of the single-argument.
 *
 * @author evitaliy
 * @since 09 May 2018
 */
public class SingleArgumentFormatterTest {

    @Test
    public void returnsSingleArgumentWhenOneGiven() {
        final String first = "123";
        final Object[] arguments = new Object[] { first, "456", "789" };
        assertEquals(new SingleArgumentFormatter().format(null, arguments), first);
    }

    @Test
    public void returnsOnlyFirstArgumentWhenManyGiven() {
        final String argument = "Abc";
        assertEquals(new SingleArgumentFormatter().format(null, argument), argument);
    }

    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void failsWhenNoArgumentsGiven() {
        new SingleArgumentFormatter().format(null);
    }

    @Test
    public void doesNotHaveValidator() {
        assertFalse(new SingleArgumentFormatter().getValidator().isPresent());
    }
}