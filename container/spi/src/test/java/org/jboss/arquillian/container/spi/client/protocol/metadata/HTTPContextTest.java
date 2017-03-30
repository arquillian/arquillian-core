/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.spi.client.protocol.metadata;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HTTPContextTest {
    static final String TEST_HOST = "localhost";
    static final int TEST_PORT = 8888;

    @Test
    public void testHTTPContext() {
        HTTPContext httpContext = new HTTPContext(TEST_HOST, TEST_PORT);

        assertEquals(TEST_HOST, httpContext.getHost());
        assertEquals(TEST_PORT, httpContext.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHTTPContextForNullHost() {
        new HTTPContext(null, TEST_PORT);
    }
}
