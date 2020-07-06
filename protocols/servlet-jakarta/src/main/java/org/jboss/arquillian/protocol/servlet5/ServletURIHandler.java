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
import java.util.Collection;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.test.api.TargetsContainer;

/**
 * ServletURIHandler
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletURIHandler {

    private ServletProtocolConfiguration config;

    private Collection<HTTPContext> contexts;

    /**
     *
     */
    public ServletURIHandler(ServletProtocolConfiguration config, Collection<HTTPContext> contexts) {
        if (config == null) {
            throw new IllegalArgumentException("ServletProtocolConfiguration must be specified");
        }
        if (contexts == null || contexts.size() == 0) {
            throw new IllegalArgumentException("HTTPContext must be specified");
        }
        this.config = config;
        this.contexts = contexts;
    }

    public URI locateTestServlet(Method method) {
        HTTPContext context = locateHTTPContext(method);
        return ServletUtil.determineBaseURI(
            config,
            context,
            ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME);
    }

    protected HTTPContext locateHTTPContext(Method method) {
        TargetsContainer targetContainer = method.getAnnotation(TargetsContainer.class);
        if (targetContainer != null) {
            String targetName = targetContainer.value();

            for (HTTPContext context : contexts) {
                if (targetName.equals(context.getName())) {
                    return context;
                }
            }
            throw new IllegalArgumentException("Could not determin HTTPContext from ProtocolMetadata for target: "
                + targetName + ". Verify that the given target name in @" + TargetsContainer.class.getSimpleName()
                + " match a name returned by the deployment container");
        }
        return contexts.toArray(new HTTPContext[] {})[0];
    }
}
