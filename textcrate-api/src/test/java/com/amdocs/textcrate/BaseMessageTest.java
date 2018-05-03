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

import com.amdocs.textcrate.CodeBlueprint.Formatting;
import com.amdocs.textcrate.api.Formatter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.testng.annotations.Test;

/**
 * Tests base class for message objects.
 *
 * @author evitaliy
 * @since 29 Jan 18
 */
public class BaseMessageTest {

    @Test
    public void notEqualWhenDifferentArguments() {
        BaseMessageBlueprint blueprint = buildStubBlueprint(0);
        assertNotEquals(new BaseMessage(blueprint, "A"), new BaseMessage(blueprint, "B"));
    }

    @Test
    public void equalWhenEqualArguments() {
        BaseMessageBlueprint blueprint = buildStubBlueprint(0);
        assertEquals(new BaseMessage(blueprint, "C"), new BaseMessage(blueprint, "C"));
    }

    @Test
    public void notEqualWhenDifferentBlueprints() {
        assertNotEquals(new BaseMessage(buildStubBlueprint(0)), new BaseMessage(buildStubBlueprint(1)));
    }

    @Test
    public void equalWhenEqualBlueprints() {
        BaseMessage one = new BaseMessage(buildStubBlueprint(0));
        BaseMessage two = new BaseMessage(buildStubBlueprint(0));
        assertEquals(one, two);
        assertEquals(one.hashCode(), two.hashCode());
    }

    @Test
    public void toStringHasCodeAndMessage() {
        assertEquals(new BaseMessage(buildStubBlueprint(0), "Hello").toString(), "[0] [Hello]");
    }

    private BaseMessageBlueprint buildStubBlueprint(int id) {
        StubFormatter formatter = new StubFormatter();
        CodeBlueprint codeBlueprint = new CodeBlueprint(id, new Formatting(0, "", formatter));
        BaseMessageBlueprint.Formatting msgFormatting = new BaseMessageBlueprint.Formatting("", formatter);
        return new BaseMessageBlueprint(codeBlueprint, msgFormatting, Collections.emptyMap());
    }

    @EqualsAndHashCode
    @ToString
    private static class StubFormatter implements Formatter {

        @Override
        public String format(String pattern, Object... arguments) {
            return Arrays.toString(arguments);
        }

        @Override
        public void validate(String pattern, Type... types) { /* no-op */ }
    }
}