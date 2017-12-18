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

import java.net.URI;

/**
 * Servlet
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 * @version $Revision: $
 */
public class Servlet {
    static final String HTTP_SCHEME = "http://";
    static final String ROOT_CONTEXT = "/";

    private final String name;

    private final String contextRoot;

    private String host;

    private int port;

    public Servlet(String name, String contextRoot) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (contextRoot == null) {
            throw new IllegalArgumentException("contextRoot must not be null");
        }

        this.name = name;
        this.contextRoot = cleanContextRoot(contextRoot);
    }

    /**
     * Set the {@link HTTPContext} for this servlet.  This is required for
     * <code>Servlet</code> to be fully initialized.  Don't use
     * {@link #equals(Object)} prior to setting the <code>HTTPContext</code>,
     * which most likely implies not adding this to a collection beforehand
     * either.
     *
     * @param context
     *     the context to set
     *
     * @throws IllegalArgumentException
     *     if the context host is null
     * @see {@link HTTPContext#add(Servlet)}
     */
    void setParent(HTTPContext context) {
        if (context.getHost() == null) {
            throw new IllegalArgumentException(context.getClass().getSimpleName() + " host must not be null");
        }
        this.host = context.getHost();
        this.port = context.getPort();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the contextRoot
     */
    public String getContextRoot() {
        return contextRoot;
    }

    /**
     * Get the URI to the Servlet's context.
     *
     * @return the base URI, e.g. "http://localhost:8888/"
     */
    public URI getBaseURI() {
        return URI.create(getBaseURIAsString());
    }

    /**
     * Get the URI to the Servlet.
     *
     * @return the base URI, e.g. "http://localhost:8888/Test"
     */
    public URI getFullURI() {
        return URI.create(getBaseURIAsString() + name);
    }

    private String getBaseURIAsString() {
        return HTTP_SCHEME + host + ":" + port + contextRoot + "/";
    }

    /**
     * @throws IllegalStateException
     *     if host is null
     * @see {@link #setParent(HTTPContext)}
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        assertHostState();

        final int prime = 31;
        int result = 1;
        result = prime * result + ((contextRoot == null) ? 0 : contextRoot.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + port;
        return result;
    }

    /**
     * @throws IllegalStateException
     *     if host is null
     * @see {@link #setParent(HTTPContext)}
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        assertHostState();

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Servlet)) {
            return false;
        }
        Servlet other = (Servlet) obj;
        if (contextRoot == null) {
            if (other.contextRoot != null) {
                return false;
            }
        } else if (!contextRoot.equals(other.contextRoot)) {
            return false;
        }
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Servlet [name=" + name + ", contextRoot=" + contextRoot + "]";
    }

    private String cleanContextRoot(String contextRoot) {
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }

        // ARQ-554
        if (contextRoot.equals(ROOT_CONTEXT)) {
            contextRoot = "";
        }

        return contextRoot;
    }

    private void assertHostState() {
        if (host == null) {
            throw new IllegalStateException("host must not be null (see setParent(...))");
        }
    }
}
