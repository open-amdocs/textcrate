/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests how unannotated messages are handled.
 *
 * @author evitaliy
 * @since 09 May 2018
 */
public class UnannotatedMessageBlueprintTest {

    private Method stubMethod;

    @BeforeClass
    public void initialize() throws NoSuchMethodException {
        stubMethod = UnannotatedMessageBlueprintTest.class.getDeclaredMethod("doNothing");
    }

    @SuppressWarnings("EmptyMethod")
    private void doNothing() {
        // do nothing, need just the declaration
    }

    @Test
    public void codeIsValidString() {
        UnannotatedMessageBlueprint blueprint = new UnannotatedMessageBlueprint(stubMethod, Collections.emptyMap());
        String code = blueprint.getCode();
        assertNotNull(code);
        assertFalse(code.trim().isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void errorWhenMethodNull() {
        new UnannotatedMessageBlueprint(null, Collections.emptyMap());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void errorWhenPropertiesNull() {
        new UnannotatedMessageBlueprint(stubMethod, null);
    }

    @Test
    public void formattedWhenArgumentsNull() {
        assertNotNull(new UnannotatedMessageBlueprint(stubMethod, Collections.emptyMap()).format(null));
    }

    @Test
    public void propertiesRetained() {
        Map<String, String> properties = new HashMap<>();
        final String key = "key";
        final String value = "VALUE";
        properties.put(key, value);
        UnannotatedMessageBlueprint blueprint = new UnannotatedMessageBlueprint(stubMethod, properties);
        assertEquals(blueprint.getProperty(key), value);
    }

    @Test
    public void patternIncludesMethodName() {
        UnannotatedMessageBlueprint blueprint = new UnannotatedMessageBlueprint(stubMethod, Collections.emptyMap());
        assertTrue(blueprint.getPattern().contains(stubMethod.getName()));
    }

    @Test
    public void formattedMessageIncludesArguments() {
        UnannotatedMessageBlueprint blueprint = new UnannotatedMessageBlueprint(stubMethod, Collections.emptyMap());
        final String argument = "XyZ";
        assertTrue(blueprint.format(new Object[] { argument }).contains(argument));
    }

    @Test
    public void formattedMessageIncludesMethodName() {
        UnannotatedMessageBlueprint blueprint = new UnannotatedMessageBlueprint(stubMethod, Collections.emptyMap());
        assertTrue(blueprint.format(new Object[0]).contains(stubMethod.getName()));
    }

    @Test
    public void formattedMessageIncludesClassName() {
        UnannotatedMessageBlueprint blueprint = new UnannotatedMessageBlueprint(stubMethod, Collections.emptyMap());
        assertTrue(blueprint.format(new Object[0]).contains(UnannotatedMessageBlueprintTest.class.getName()));
    }
}