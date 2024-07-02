/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.arquillian.integration.test.common;

import java.net.URI;

/**
 * The test environment setup.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TestEnvironment {

    private static final String PROTOCOL = System.getProperty("arq.protocol", "http");
    private static final String HOST = System.getProperty("arq.host", "127.0.0.1");
    private static final int PORT = Integer.parseInt(System.getProperty("arq.port", "8080"));
    public static final String REST_PATH = "/rest";

    private TestEnvironment() {
    }

    /**
     * Returns the defined protocol to use for HTTP connections. The default is {@code http} and can be overridden
     * with the {@code arq.protocol} system property.
     *
     * @return the HTTP protocol
     */
    public static String protocol() {
        return PROTOCOL;
    }

    /**
     * Returns the defined host to use for HTTP connections. The default is {@code 127.0.0.1} and can be overridden
     * with the {@code arq.host} system property.
     *
     * @return the HTTP host
     */
    public static String host() {
        return HOST;
    }

    /**
     * Returns the defined port to use for HTTP connections. The default is {@code 8080} and can be overridden
     * with the {@code arq.port} system property.
     *
     * @return the HTTP port
     */
    public static int port() {
        return PORT;
    }

    /**
     * Creates a URI with the given paths appended to the {@linkplain #protocol() protocol}, {@linkplain #host() host}
     * and {@linkplain #port() port}.
     *
     * @param paths the paths to append
     *
     * @return a new URI for an HTTP connection
     */
    public static URI uri(final String... paths) {
        final StringBuilder uri = new StringBuilder()
            .append(protocol())
            .append("://")
            .append(host())
            .append(':')
            .append(port());
        for (String path : paths) {
            if (!path.isEmpty()) {
                if (path.charAt(0) == '/') {
                    uri.append(path);
                } else {
                    uri.append('/').append(path);
                }
            }
        }
        return URI.create(uri.toString());
    }

}
