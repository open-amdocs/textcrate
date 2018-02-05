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

/**
 * <p>Defines the metadata and formatting rules for a message, but does not include its arguments.
 * An instance of {@link MessageBlueprint} should be used to construct messages that differ only in their arguments.</p>
 *
 * @author evitaliy
 * @since 31 Jan 18
 */
interface MessageBlueprint {

    String format(Object[] arguments);

    String getPattern();

    String getCode();

    String getProperty(String name);
}
