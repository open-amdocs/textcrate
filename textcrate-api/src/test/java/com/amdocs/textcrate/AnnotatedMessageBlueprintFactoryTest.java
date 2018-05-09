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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.amdocs.textcrate.api.Formatter;
import com.amdocs.textcrate.api.Validator;
import com.amdocs.textcrate.api.annotations.CodeSpec;
import com.amdocs.textcrate.api.annotations.MessageFormatter;
import com.amdocs.textcrate.api.annotations.MessageProperty;
import com.amdocs.textcrate.api.annotations.MessageSpec;
import com.amdocs.textcrate.formatters.ToStringFormatter;
import java.lang.annotation.Annotation;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.testng.annotations.Test;

/**
 * Test formatting when messages are properly annotated.
 *
 * @author evitaliy
 * @since 09 May 2018
 */
public class AnnotatedMessageBlueprintFactoryTest {

    private static final String CUSTOM_CODE_PATTERN = "[APP]:{}-code";
    private static final int CUSTOM_OFFSET = 1000;
    private static final String PROPERTY_A_KEY = "A";
    private static final String PROPERTY_A_VALUE = "a";
    private static final String PROPERTY_B_KEY = "B";
    private static final String PROPERTY_B_VALUE = "b";

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Class.*")
    public void factoryCannotBeCreatedWhenClassNull() {
        new AnnotatedMessageBlueprintFactory(null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Annotation.*")
    public void blueprintCannotBeCreatedWhenInputNull() {
        new AnnotatedMessageBlueprintFactory(this.getClass()).createBlueprint(null);
    }

    @Test
    public void codeUnchangedWhenNoCodeAnnotationOnClass() {
        AnnotatedMessageBlueprintFactory factory = new AnnotatedMessageBlueprintFactory(this.getClass());
        MessageBlueprint blueprint = factory.createBlueprint(new MessageSpecAnnotationMock("", 100));
        assertEquals(blueprint.getCode(), "100");
    }

    @Test
    public void codeSpecAppliedWhenCodeAnnotationPresent() {

        AnnotatedMessageBlueprintFactory factory =
                new AnnotatedMessageBlueprintFactory(CodeSpecAnnotatedInterface.class);

        final int code = 56;

        MessageBlueprint blueprint = factory.createBlueprint(new MessageSpecAnnotationMock("", code));
        Formatter defaultFormatter = MessageFormatter.DEFAULT_FORMATTER;
        assertEquals(blueprint.getCode(), defaultFormatter.format(CUSTOM_CODE_PATTERN, CUSTOM_OFFSET + code));
    }

    @Test
    public void correctMessageReturnedWhenDefaultFormatter() {

        AnnotatedMessageBlueprintFactory factory = new AnnotatedMessageBlueprintFactory(this.getClass());

        final String pattern = "Hello, {}!";
        final String argument = "world";

        MessageBlueprint blueprint = factory.createBlueprint(new MessageSpecAnnotationMock(pattern, 0));
        assertEquals(blueprint.getPattern(), pattern);
        Formatter defaultFormatter = MessageFormatter.DEFAULT_FORMATTER;
        assertEquals(blueprint.format(new Object[] {argument}), defaultFormatter.format(pattern, argument));
    }

    @Test
    public void customFormatAppliedToCodeWhenCustomMessageFormatterSpecified() {

        AnnotatedMessageBlueprintFactory factory =
                new AnnotatedMessageBlueprintFactory(CustomFormatterAndCodePatternAnnotatedInterface.class);

        final int code = 120;

        MessageBlueprint blueprint = factory.createBlueprint(new MessageSpecAnnotationMock("Just a message", code));
        ToStringFormatter expectedFormatter = new ToStringFormatter();
        assertEquals(blueprint.getCode(), expectedFormatter.format(CUSTOM_CODE_PATTERN, code));
    }

    @Test
    public void customFormatAppliedToMessageWhenCustomFormatterSpecified() {

        AnnotatedMessageBlueprintFactory factory =
                new AnnotatedMessageBlueprintFactory(CustomFormatterAnnotatedInterface.class);

        final String pattern = "JUST PATTERN";
        final int argument = 77;

        MessageBlueprint blueprint = factory.createBlueprint(new MessageSpecAnnotationMock(pattern, 0));
        ToStringFormatter expectedFormatter = new ToStringFormatter();
        assertEquals(blueprint.format(new Object[] {argument}), expectedFormatter.format(pattern, argument));
    }

    @Test
    public void fallsBackToSimpleFormatterWhenCannotInstantiateCustom() {
        AnnotatedMessageBlueprintFactory factory =
                new AnnotatedMessageBlueprintFactory(NonInstantiableFormatterAnnotatedInterface.class);
        final String pattern = "XYZ";
        final Formatter fallback = new ToStringFormatter();
        MessageBlueprint blueprint = factory.createBlueprint(new MessageSpecAnnotationMock(pattern, 0));
        assertEquals(blueprint.format(new Object[0]), fallback.format(pattern));
    }

    @Test
    public void nullReturnedWhenNoPropertyAnnotation() {
        AnnotatedMessageBlueprintFactory factory =
                new AnnotatedMessageBlueprintFactory(this.getClass());
        MessageBlueprint blueprint = factory.createBlueprint(new MessageSpecAnnotationMock(null, 0));
        assertNull(blueprint.getProperty(PROPERTY_A_KEY));
        assertTrue(factory.getProperties().isEmpty());
    }

    @Test
    public void propertiesReturnedWhenMultiplePropertyAnnotations() {
        AnnotatedMessageBlueprintFactory factory =
                new AnnotatedMessageBlueprintFactory(NonInstantiableFormatterAnnotatedInterface.class);
        MessageBlueprint blueprint = factory.createBlueprint(new MessageSpecAnnotationMock(null, 0));
        assertEquals(blueprint.getProperty(PROPERTY_A_KEY), PROPERTY_A_VALUE);
        assertEquals(blueprint.getProperty(PROPERTY_B_KEY), PROPERTY_B_VALUE);
        assertEquals(factory.getProperties().size(), 2);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void propertiesUnmodifiable() {
        AnnotatedMessageBlueprintFactory factory =
                new AnnotatedMessageBlueprintFactory(this.getClass());
        factory.getProperties().put("Modification", "Attempt");
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static class MessageSpecAnnotationMock implements MessageSpec {

        private final String pattern;
        private final int id;

        @Override
        public String pattern() {
            return pattern;
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return MessageSpec.class;
        }
    }

    @CodeSpec(pattern = CUSTOM_CODE_PATTERN, offset = CUSTOM_OFFSET)
    private interface CodeSpecAnnotatedInterface {
        // only for the class-level annotation
    }

    @MessageFormatter(type = ToStringFormatter.class)
    private interface CustomFormatterAnnotatedInterface {
        // only for the class-level annotations
    }

    @MessageFormatter(type = NonInstantiableFormatter.class)
    @MessageProperty(name = PROPERTY_A_KEY, value = PROPERTY_A_VALUE)
    @MessageProperty(name = PROPERTY_B_KEY, value = PROPERTY_B_VALUE)
    private interface NonInstantiableFormatterAnnotatedInterface {
        // only for the class-level annotation
    }

    @MessageFormatter(type = ToStringFormatter.class)
    @CodeSpec(pattern = CUSTOM_CODE_PATTERN)
    private interface CustomFormatterAndCodePatternAnnotatedInterface {
        // only for the class-level annotations
    }

    private static class NonInstantiableFormatter implements Formatter {

        NonInstantiableFormatter() {
            throw new UnsupportedOperationException("Cannot be instantiated");
        }

        @Override
        public String format(String pattern, Object... arguments) {
            throw new UnsupportedOperationException("Unexpected call");
        }

        @Override
        public Optional<Validator> getValidator() {
            return Optional.empty();
        }
    }
}