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

import java.util.Collection;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;

/**
 * BaseServletProtocol
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
public abstract class BaseServletProtocol implements Protocol<ServletProtocolConfiguration> {
    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.client.protocol.Protocol#getProtocolConfigurationClass()
     */
    @Override
    public Class<ServletProtocolConfiguration> getProtocolConfigurationClass() {
        return ServletProtocolConfiguration.class;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.client.protocol.Protocol#getDescription()
     */
    @Override
    public ProtocolDescription getDescription() {
        return new ProtocolDescription(getProtocolName());
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.client.protocol.Protocol#getExecutor(org.jboss.arquillian.spi.client.protocol.ProtocolConfiguration, org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData)
     */
    @Override
    public ServletMethodExecutor getExecutor(ServletProtocolConfiguration protocolConfiguration,
        ProtocolMetaData metaData, CommandCallback callback) {
        Collection<HTTPContext> contexts = metaData.getContexts(HTTPContext.class);
        if (contexts.size() == 0) {
            throw new IllegalArgumentException(
                "No " + HTTPContext.class.getName() + " found in " + ProtocolMetaData.class.getName() + ". " +
                    "Servlet protocol can not be used");
        }
        return new ServletMethodExecutor(protocolConfiguration, contexts, callback);
    }

    protected abstract String getProtocolName();
}
