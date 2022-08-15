/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WebContext
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 * @version $Revision: $
 */
public class HTTPContext extends NamedContext {
    private final String host;
    private final int port;
    private final String scheme;

    private final Set<Servlet> servlets;

    public HTTPContext(String host, int port) {
        this("no-named", host, port, false);
    }

    public HTTPContext(String host, int port, boolean secure) {
        this("no-named", host, port, secure);
    }

    public HTTPContext(String name, String host, int port) {
        this(name, host, port, false);
    }

    /**
     * Creates a new HTTP context.
     *
     * @param name   the name for the context
     * @param host   the host for the context
     * @param port   the port for the context
     * @param secure whether this is a secure context. {@code true} will result in a https scheme, otherwise http will
     *               be used for the scheme
     */
    public HTTPContext(String name, String host, int port, boolean secure) {
        super(name);

        if (host == null) {
            throw new IllegalArgumentException("host must not be null");
        }
        this.host = host;
        this.port = port;
        this.scheme = secure ? "https" : "http";
        this.servlets = new HashSet<Servlet>();
    }

    /**
     * @return the ip
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    public HTTPContext add(Servlet servlet) {
        servlet.setParent(this);
        this.servlets.add(servlet);
        return this;
    }

    /**
     * @return the servlets
     */
    public List<Servlet> getServlets() {
        return new ArrayList<Servlet>(servlets);
    }

    public Servlet getServletByName(String name) {
        for (Servlet servlet : getServlets()) {
            if (servlet.getName().equals(name)) {
                return servlet;
            }
        }
        return null;
    }

    /**
     * Returns the scheme used for the connection.
     *
     * @return the scheme used for the connection
     */
    public String getScheme() {
        return scheme;
    }

    @Override
    public String toString() {
        return "HTTPContext [scheme=" + scheme + ", host=" + host + ", port=" + port + ", servlets=" + toString(servlets) + "]";
    }

    private String toString(Set<Servlet> servlets) {
        StringBuilder sb = new StringBuilder();
        if (servlets != null) {
            for (Object obj : servlets) {
                sb.append('\n').append(obj);
            }
        }
        return sb.toString();
    }
}
