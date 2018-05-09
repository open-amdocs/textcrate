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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.amdocs.textcrate.spi.MessagesProvider;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.testng.annotations.Test;

/**
 * Tests loading a message repository defined in an interface.
 *
 * @author evitaliy
 * @since 25 Jan 18
 */
public class MessagesTest {

    private static final String PROXY_CLASS_PREFIX = ".$Proxy";

    @Test(expectedExceptions = NullPointerException.class)
    public void loadingMessagesThrowsNpeWhenClassNull() {
        Messages.from(null);
    }

    @Test
    public void loadingMessagesAlwaysReturnsSomething() {
        assertNotNull(Messages.from(MockMessages.class));
    }

    @Test
    public void loadingMessagesReturnsDynamicProxyWhenNoProviderConfigured() {
        String implementationClass = Messages.from(MockMessages.class).getClass().getName();
        assertTrue(implementationClass.startsWith(this.getClass().getPackage().getName() + PROXY_CLASS_PREFIX));
    }

    @Test
    public void loadingMessagesReturnsCustomImplementationWhenAvailable() {

        final String providerInterface = "META-INF/services/" + MessagesProvider.class.getName();
        final byte[] implementationName = MockMessagesProvider.class.getName().getBytes(StandardCharsets.UTF_8);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ServiceHelperClassLoader classLoader =
                new ServiceHelperClassLoader(providerInterface, implementationName, contextClassLoader);

        assertEquals(Messages.from(MockMessages.class, classLoader).getClass(), MockMessagesImpl.class);
    }

    @Test
    public void loadingMessagesReturnsDynamicProxyWhenCustomProviderReturnsNothing() {

        final String providerInterface = "META-INF/services/" + MessagesProvider.class.getName();
        final byte[] implementationName = NoMessagesProvider.class.getName().getBytes(StandardCharsets.UTF_8);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ServiceHelperClassLoader classLoader =
                new ServiceHelperClassLoader(providerInterface, implementationName, contextClassLoader);

        String implementationClass = Messages.from(MockMessages.class, classLoader).getClass().getName();
        assertTrue(implementationClass.startsWith(this.getClass().getPackage().getName() + PROXY_CLASS_PREFIX));
    }

    private interface MockMessages { /* methods not needed */}

    private static class MockMessagesImpl implements MockMessages { /* not needed */ }

    @SuppressWarnings("WeakerAccess")
    public static class MockMessagesProvider implements MessagesProvider {

        @Override
        public <T> Optional<T> getMessages(Class<T> clazz) {
            return Optional.of(clazz.cast(new MockMessagesImpl()));
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class NoMessagesProvider implements MessagesProvider {

        @Override
        public <T> Optional<T> getMessages(Class<T> clazz) {
            return Optional.empty();
        }
    }
}