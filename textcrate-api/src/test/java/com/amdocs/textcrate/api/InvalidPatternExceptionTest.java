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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/**
 * @author evitaliy
 * @since 29 Jan 18
 */
public class InvalidPatternExceptionTest {

    @Test
    public void messageCorrectWhenConstructedWithMessage() {
        final String msg = "Invalid without cause";
        assertEquals(new InvalidPatternException(msg).getMessage(), msg);
    }

    @Test
    public void causeCorrectWhenConstructedWithMessage() {
        final Throwable cause = new UnsupportedOperationException("Not supported");
        assertEquals(new InvalidPatternException("", cause).getCause(), cause);
    }

    @Test
    public void causeNullWhenConstructedWithoutCause() {
        assertNull(new InvalidPatternException("").getCause());
    }
}