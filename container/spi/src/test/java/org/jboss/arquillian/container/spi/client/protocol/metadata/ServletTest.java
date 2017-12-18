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

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ServletTest {
    private static final String TEST_SERVLET_NAME = "jsp";

    private static final String TEST_CONTEXT_ROOT = "/test";

    private static final String ROOT_CONTEXT_ROOT_BASE_URI = Servlet.HTTP_SCHEME + HTTPContextTest.TEST_HOST + ":"
        + HTTPContextTest.TEST_PORT + "/";

    private static final String TEST_CONTEXT_ROOT_BASE_URI = ROOT_CONTEXT_ROOT_BASE_URI + TEST_CONTEXT_ROOT + "/";

    private static final HTTPContext TEST_HTTP_CONTEXT = new HTTPContext(HTTPContextTest.TEST_HOST,
        HTTPContextTest.TEST_PORT);

    private static Servlet servlet;

    private static Servlet servletWithParent;

    private static Servlet rootContextServletWithParent;

    @BeforeClass
    public static void before() {
        servlet = new Servlet(TEST_SERVLET_NAME, TEST_CONTEXT_ROOT);
        servletWithParent = new Servlet(TEST_SERVLET_NAME, TEST_CONTEXT_ROOT);
        servletWithParent.setParent(TEST_HTTP_CONTEXT);
        rootContextServletWithParent = new Servlet(TEST_SERVLET_NAME, Servlet.ROOT_CONTEXT);
        rootContextServletWithParent.setParent(TEST_HTTP_CONTEXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServletForNullName() {
        new Servlet(null, TEST_CONTEXT_ROOT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServletForNullContextRoot() {
        new Servlet(TEST_SERVLET_NAME, null);
    }

    @Test
    public void testGetBaseURIForTestContext() {
        final String actualBaseUri = servletWithParent.getBaseURI().toString();
        assertEquals(TEST_CONTEXT_ROOT_BASE_URI, actualBaseUri);
    }

    /**
     * ARQ-554
     */
    @Test
    public void testGetBaseURIForRootContext() {
        final String actualBaseUri = rootContextServletWithParent.getBaseURI().toString();
        assertEquals(ROOT_CONTEXT_ROOT_BASE_URI, actualBaseUri);
    }

    @Test(expected = IllegalStateException.class)
    public void testEqualsObjectForNullHost() {
        servlet.equals(null);
    }

    @Test
    public void testEqualsObjectForNonNullHost() {
        try {
            assertTrue(servletWithParent.equals(servletWithParent));
        } catch (IllegalStateException e) {
            failOnUnexpectedException(e);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testHashCodeForNullHost() {
        servlet.hashCode();
    }

    @Test
    public void testHashCodeForNonNullHost() {
        try {
            servletWithParent.hashCode();
        } catch (IllegalStateException e) {
            failOnUnexpectedException(e);
        }
    }

    private void failOnUnexpectedException(Exception e) {
        fail(e.getClass().getSimpleName() + " was not expected: " + e.getMessage());
    }
}
