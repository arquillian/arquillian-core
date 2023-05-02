/*
 * Copyright 2022 Red Hat Inc. and/or its affiliates and other contributors
 * identified by the Git commit log. 
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
package org.jboss.arquillian.protocol.rest;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.protocol.servlet5.ServletProtocolConfiguration;
import org.jboss.arquillian.protocol.servlet5.ServletURIHandler;
import org.jboss.arquillian.protocol.servlet5.ServletUtil;

public class RESTURIHandler extends ServletURIHandler {

    private ServletProtocolConfiguration config;
    public RESTURIHandler(ServletProtocolConfiguration config, Collection<HTTPContext> contexts) {
        super(config, contexts);
        this.config = config;
    }

    public URI locateTestServlet(Method method) {
        HTTPContext context = locateHTTPContext(method);
        return ServletUtil.determineBaseURI(
            config,
            context,
            RESTMethodExecutor.ARQUILLIAN_REST_NAME);
    }
}
