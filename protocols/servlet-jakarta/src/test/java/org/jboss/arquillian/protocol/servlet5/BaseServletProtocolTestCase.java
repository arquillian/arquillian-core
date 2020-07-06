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
package org.jboss.arquillian.protocol.servlet5;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.junit.Assert;
import org.junit.Test;

/**
 * ServletProtocolTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class BaseServletProtocolTestCase {
    @Test
    public void shouldFindTestServletInMetadata() throws Exception {
        ServletProtocolConfiguration config = new ServletProtocolConfiguration();

        HTTPContext testContext = new HTTPContext("127.0.0.1", 8080)
            .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "test"));

        Method testMethod = getTestMethod("testNoAnnotations");

        ServletURIHandler handler = new ServletURIHandler(config, to(testContext));
        URI result = handler.locateTestServlet(testMethod);

        Assert.assertEquals("http://127.0.0.1:8080/test", result.toString());
    }

    @Test
    public void shouldOverrideMetadata() throws Exception {
        ServletProtocolConfiguration config = new ServletProtocolConfiguration();
        config.setScheme("https");
        config.setHost("10.10.10.1");
        config.setPort(90);

        HTTPContext testContext = new HTTPContext("127.0.0.1", 8080)
            .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "test"));

        Method testMethod = getTestMethod("testNoAnnotations");

        ServletURIHandler handler = new ServletURIHandler(config, to(testContext));
        URI result = handler.locateTestServlet(testMethod);

        Assert.assertEquals("https://10.10.10.1:90/test", result.toString());
    }

    @Test
    public void shouldMatchNamedTargetedContext() throws Exception {
        ServletProtocolConfiguration config = new ServletProtocolConfiguration();

        HTTPContext testContextOne = new HTTPContext("Y", "127.0.0.1", 8080)
            .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "test"));

        HTTPContext testContextTwo = new HTTPContext("X", "127.0.0.1", 8081)
            .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "test"));

        Method testMethod = getTestMethod("testTargeted");

        ServletURIHandler handler = new ServletURIHandler(config, to(testContextOne, testContextTwo));
        URI result = handler.locateTestServlet(testMethod);

        Assert.assertEquals("http://127.0.0.1:8081/test", result.toString());
    }

    @Test
    public void shouldOverrideMatchNamedTargetedContext() throws Exception {
        ServletProtocolConfiguration config = new ServletProtocolConfiguration();
        config.setScheme("https");
        config.setHost("10.10.10.1");
        config.setPort(90);

        HTTPContext testContextOne = new HTTPContext("Y", "127.0.0.1", 8080)
            .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "testY"));

        HTTPContext testContextTwo = new HTTPContext("X", "127.0.0.1", 8081)
            .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "testX"));

        Method testMethod = getTestMethod("testTargeted");

        ServletURIHandler handler = new ServletURIHandler(config, to(testContextOne, testContextTwo));
        URI result = handler.locateTestServlet(testMethod);

        Assert.assertEquals("https://10.10.10.1:90/testX", result.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingNamedTargetedContext() throws Exception {
        ServletProtocolConfiguration config = new ServletProtocolConfiguration();

        HTTPContext testContextOne = new HTTPContext("Y", "127.0.0.1", 8080)
            .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "test"));

        Method testMethod = getTestMethod("testTargeted");

        ServletURIHandler handler = new ServletURIHandler(config, to(testContextOne));
        handler.locateTestServlet(testMethod);
    }

    private Collection<HTTPContext> to(HTTPContext... inputs) {
        List<HTTPContext> contexts = new ArrayList<HTTPContext>();
        Collections.addAll(contexts, inputs);
        return contexts;
    }

    private Method getTestMethod(String methodName) throws Exception {
        return getClass().getDeclaredMethod(methodName);
    }


   /*
    * Methods used for ServletURIHandler HTTPContext lookups. 
    */

    @SuppressWarnings("unused")
    private void testNoAnnotations() {
    }

    @TargetsContainer("X")
    private void testTargeted() {
    }
}
