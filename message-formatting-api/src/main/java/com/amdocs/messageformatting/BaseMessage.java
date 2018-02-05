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

import com.amdocs.messageformatting.api.Message;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * <p>Private to this package implementation of {@link Message}. It is based on a {@link MessageBlueprint} for
 * formatting rules, and arguments used to construct a particular message.</p>
 *
 * @author evitaliy
 * @since 18 Oct 17
 */
class BaseMessage implements Message {

    private final Object[] arguments;
    private final MessageBlueprint blueprint;

    BaseMessage(MessageBlueprint blueprint, Object... arguments) {
        this.blueprint = blueprint;
        this.arguments = arguments;
    }

    @Override
    public String getPattern() {
        return blueprint.getPattern();
    }

    @Override
    public String getPattern(Locale locale) {
        return getPattern(); // ignore the locale for now
    }

    @Override
    public String getMessage() {
        return blueprint.format(arguments);
    }

    @Override
    public String getMessage(Locale locale) {
        return getMessage(); // ignore the locale for now
    }

    @Override
    public Object[] getArguments() {
        return Arrays.copyOf(arguments, arguments.length);
    }

    @Override
    public String getCode() {
        return blueprint.getCode();
    }

    @Override
    public String getProperty(String name) {
        return blueprint.getProperty(name);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseMessage message = (BaseMessage) o;
        return Arrays.equals(arguments, message.arguments) && Objects.equals(blueprint, message.blueprint);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(blueprint);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    @Override
    public String toString() {
        return "BaseMessage {arguments=" + Arrays.toString(arguments) + ", blueprint=" + blueprint + '}';
    }
}
