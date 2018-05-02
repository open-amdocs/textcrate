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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows loading resources from memory via a custom class loader and URL protocol.
 *
 * @author evitaliy
 * @since 15 Jan 18
 */
public class ServiceHelperClassLoader extends ClassLoader {

    private static final String PROTOCOL = "memory";
    private static final String URL_PREFIX = PROTOCOL + "://";

    private static final InMemoryURLStreamHandler HANDLER = new InMemoryURLStreamHandler();

    static {
        // register the protocol
        URL.setURLStreamHandlerFactory(protocol -> PROTOCOL.equals(protocol) ? HANDLER : null);
    }

    public ServiceHelperClassLoader(String resourceName, byte[] resourceContent, ClassLoader parent) {
        super(parent);
        HANDLER.add(resourceName, resourceContent);
    }

    public ServiceHelperClassLoader(Map<String, byte[]> resources, ClassLoader parent) {
        super(parent);
        HANDLER.addAll(resources);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return HANDLER.contains(name)
                       ? Collections.enumeration(Collections.singletonList(new URL(URL_PREFIX + name)))
                       : Collections.emptyEnumeration();
    }

    private static class InMemoryURLStreamHandler extends URLStreamHandler {

        private final Map<String, byte[]> resources = new ConcurrentHashMap<>();

        private void add(String name, byte[] content) {
            resources.put(name, content);
        }

        private void addAll(Map<String, byte[]> added) {
            this.resources.putAll(added);
        }

        byte[] get(String name) {
            return resources.get(name);
        }

        boolean contains(String name) {
            return resources.containsKey(name);
        }

        @Override
        protected URLConnection openConnection(URL url) {
            String name = Objects.requireNonNull(url, "URL cannot be null")
                                 .toExternalForm().substring(URL_PREFIX.length());
            byte[] content = resources.get(name);
            return content == null ? null : new InMemoryURLConnection(url, content);
        }
    }

    private static class InMemoryURLConnection extends URLConnection {

        private final byte[] content;

        private InMemoryURLConnection(URL url, byte[] content) {
            super(url);
            this.content = content;
        }

        @Override
        public void connect() {
            // nothing to do here
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }
    }
}
