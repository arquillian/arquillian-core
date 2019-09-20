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
package org.jboss.arquillian.protocol.servlet;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.protocol.servlet.runner.ServletTestRunner;
import org.jboss.arquillian.protocol.servlet.test.MockTestRunner;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.junit.After;
import org.junit.Before;

/**
 * AbstractServerBase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class AbstractServerBase {
    private Server server;

    @Before
    public void setup() throws Exception {
        server = new Server(getAvailableLocalPort());

        ServletContextHandler root = new ServletContextHandler(ServletContextHandler.SESSIONS);
        root.setContextPath("/arquillian-protocol");
        root.addServlet(ServletTestRunner.class, ServletMethodExecutor.ARQUILLIAN_SERVLET_MAPPING);
        server.setHandler(root);
        server.start();
    }

    @After
    public void cleanup() throws Exception {
        MockTestRunner.clear();
        server.stop();
    }

    private int getAvailableLocalPort() throws IOException {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    protected Collection<HTTPContext> createContexts() {
        List<HTTPContext> context = new ArrayList<HTTPContext>();
        context.add(createContext());
        return context;
    }

    protected HTTPContext createContext() {
        URI baseURI = createBaseURL();
        HTTPContext context = new HTTPContext(baseURI.getHost(), baseURI.getPort());
        context.add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, baseURI.getPath()));
        return context;
    }

    protected URI createBaseURL() {
        return URI.create("http://localhost:" + server.getConnectors()[0].getPort() + "/arquillian-protocol");
    }

    protected URL createURL(String outputMode, String testClass, String methodName) {
        StringBuilder url = new StringBuilder(createBaseURL().toASCIIString() + "/ArquillianServletRunner");
        boolean first = true;
        if (outputMode != null) {
            if (first) {
                first = false;
                url.append("?");
            } else {
                url.append("&");
            }
            url.append(ServletTestRunner.PARA_OUTPUT_MODE).append("=").append(outputMode);
        }
        if (testClass != null) {
            if (first) {
                first = false;
                url.append("?");
            } else {
                url.append("&");
            }
            url.append(ServletTestRunner.PARA_CLASS_NAME).append("=").append(testClass);
        }
        if (methodName != null) {
            if (first) {
                first = false;
                url.append("?");
            } else {
                url.append("&");
            }
            url.append(ServletTestRunner.PARA_METHOD_NAME).append("=").append(methodName);
        }

        try {
            return new URL(url.toString());
        } catch (Exception e) {
            throw new RuntimeException("Could not create url", e);
        }
    }

    public static class MockTestExecutor implements TestMethodExecutor, Serializable {

        private static final long serialVersionUID = 1L;

        public void invoke(Object... parameters) throws Throwable {
        }

        public String getMethodName() {
            return getMethod().getName();
        }

        public Method getMethod() {
            try {
                return this.getClass().getMethod("getMethod");
            } catch (Exception e) {
                throw new RuntimeException("Could not find my own method ?? ");
            }
        }

        public Object getInstance() {
            return this;
        }
    }
}
