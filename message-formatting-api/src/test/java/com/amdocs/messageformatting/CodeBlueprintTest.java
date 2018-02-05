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

package com.amdocs.messageformatting;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.amdocs.messageformatting.formatters.Slf4jFormatter;
import org.testng.annotations.Test;

/**
 * @author evitaliy
 * @since 29 Jan 18
 */
public class CodeBlueprintTest {

    @Test
    public void notEqualWhenIdsDifferent() {
        assertNotEquals(new CodeBlueprint(0, null), new CodeBlueprint(1, null));
    }

    @Test
    public void notEqualWhenOffsetDifferent() {
        assertNotEquals(new CodeBlueprint(0, new CodeBlueprint.Formatting(0, null, null)),
                new CodeBlueprint(0, new CodeBlueprint.Formatting(1, null, null)));
    }

    @Test
    public void notEqualWhenPatternsDifferent() {
        assertNotEquals(new CodeBlueprint(2, new CodeBlueprint.Formatting(100, "%d", null)),
                new CodeBlueprint(2, new CodeBlueprint.Formatting(100, "{}", null)));
    }

    @Test
    public void notEqualWhenFormattersDifferent() {
        assertNotEquals(new CodeBlueprint(2, new CodeBlueprint.Formatting(100, "{}", null)),
                new CodeBlueprint(2, new CodeBlueprint.Formatting(100, "{}", new Slf4jFormatter())));
    }

    @Test
    public void equalWhenAllFieldsEqual() {
        CodeBlueprint one = new CodeBlueprint(3, new CodeBlueprint.Formatting(100, "{}", new Slf4jFormatter()));
        CodeBlueprint two = new CodeBlueprint(3, new CodeBlueprint.Formatting(100, "{}", new Slf4jFormatter()));
        assertEquals(one, two);
        assertEquals(one.hashCode(), two.hashCode());
    }

}