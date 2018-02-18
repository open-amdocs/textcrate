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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collections;
import org.testng.annotations.Test;

/**
 * @author evitaliy
 * @since 29 Jan 18
 */
public class BaseMessageTest {

    @Test
    public void notEqualWhenDifferentArguments() {
        assertNotEquals(new BaseMessage(null, "A"), new BaseMessage(null, "B"));
    }

    @Test
    public void equalWhenEqualArguments() {
        assertEquals(new BaseMessage(null, "C"), new BaseMessage(null, "C"));
    }

    @Test
    public void notEqualWhenDifferentBlueprints() {
        assertNotEquals(new BaseMessage(
                new BaseMessageBlueprint(new CodeBlueprint(0, null), null, Collections.emptyMap())
        ), new BaseMessage(
                new BaseMessageBlueprint(new CodeBlueprint(1, null), null, Collections.emptyMap())
        ));
    }

    @Test
    public void equalWhenEqualBlueprints() {
        BaseMessage one = new BaseMessage(new BaseMessageBlueprint(
                new CodeBlueprint(0, null), null, Collections.emptyMap()));
        BaseMessage two = new BaseMessage(new BaseMessageBlueprint(
                new CodeBlueprint(0, null), null, Collections.emptyMap()));
        assertEquals(one, two);
        assertEquals(one.hashCode(), two.hashCode());
    }
}