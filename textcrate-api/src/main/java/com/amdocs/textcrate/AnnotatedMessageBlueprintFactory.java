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
import com.amdocs.textcrate.api.annotations.CodeSpec;
import com.amdocs.textcrate.api.annotations.MessageFormatter;
import com.amdocs.textcrate.api.annotations.MessageProperty;
import com.amdocs.textcrate.api.annotations.MessageSpec;
import com.amdocs.textcrate.formatters.ToStringFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constructs message blueprints from annotated methods of an annotated class.
 *
 * @author evitaliy
 * @since 08 May 2018
 */
@EqualsAndHashCode
@ToString
class AnnotatedMessageBlueprintFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedMessageBlueprintFactory.class);

    /**
     * Return something meaningful when all other attempts failed.
     */
    private static final Formatter FALLBACK_FORMATTER = new ToStringFormatter();

    /**
     * Assume {@link MessageFormatter#DEFAULT_FORMATTER} as default, but if it fails to format a message use fallback.
     */
    private static final Formatter DEFAULT_MESSAGE_FORMATTER =
            new ResilientFormatter(MessageFormatter.DEFAULT_FORMATTER, FALLBACK_FORMATTER);

    /**
     * Should be used when no message code pattern was specified. Otherwise, an "if" statement would be needed to check
     * the code formatter for <code>null</code> every time a message code is requested.
     */
    private static final Formatter DEFAULT_MSG_CODE_FORMATTER = new SingleArgumentFormatter();

    private final Formatter messageFormatter;
    private final CodeBlueprint.Formatting codeFormatting;
    private final Map<String, String> properties;

    AnnotatedMessageBlueprintFactory(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        this.messageFormatter = initMessageFormatter(clazz);
        this.codeFormatting = initCodeFormatting(clazz, this.messageFormatter);
        this.properties = initProperties(clazz);
    }

    MessageBlueprint createBlueprint(MessageSpec annotation) {

        Objects.requireNonNull(annotation, "Annotation cannot be null");

        BaseMessageBlueprint.Formatting formatting =
                new BaseMessageBlueprint.Formatting(annotation.pattern(), this.messageFormatter);
        CodeBlueprint codeBlueprint = new CodeBlueprint(annotation.id(), this.codeFormatting);
        return new BaseMessageBlueprint(codeBlueprint, formatting, properties);
    }

    private Formatter initMessageFormatter(Class<?> clazz) {

        MessageFormatter formatAnnotation = clazz.getAnnotation(MessageFormatter.class);
        if (formatAnnotation == null) {
            LOGGER.debug("No format annotation on {}. Default will be used", clazz.getName());
            return DEFAULT_MESSAGE_FORMATTER;
        }

        try {
            Formatter requestedFormatter = formatAnnotation.type().newInstance();
            return new ResilientFormatter(requestedFormatter, FALLBACK_FORMATTER);
        } catch (Exception e) {
            LOGGER.error("Formatter " + formatAnnotation.type() + " could not be instantiated", e);
            return FALLBACK_FORMATTER;
        }
    }

    private CodeBlueprint.Formatting initCodeFormatting(Class<?> clazz, Formatter formatter) {

        CodeSpec codeAnnotation = clazz.getAnnotation(CodeSpec.class);
        if (codeAnnotation == null) {
            LOGGER.debug("No message code annotation on {}. Default will be used", clazz.getName());
            return new CodeBlueprint.Formatting(0, "", DEFAULT_MSG_CODE_FORMATTER);
        } else {
            return new CodeBlueprint.Formatting(codeAnnotation.offset(), codeAnnotation.pattern(), formatter);
        }
    }

    private Map<String, String> initProperties(Class<?> clazz) {
        MessageProperty[] propsAnnotation = clazz.getAnnotationsByType(MessageProperty.class);
        return propsAnnotation.length == 0 ? Collections.emptyMap()
                       : Arrays.stream(propsAnnotation)
                               .collect(Collectors.toMap(MessageProperty::name, MessageProperty::value));
    }

    Map<String, String> getProperties() {
        return properties;
    }

}
