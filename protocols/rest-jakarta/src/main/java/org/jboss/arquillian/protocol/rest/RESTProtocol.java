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

import java.util.Collection;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.protocol.servlet5.ServletProtocolConfiguration;

public class RESTProtocol implements Protocol<ServletProtocolConfiguration> {
    public static final String PROTOCOL_NAME = "REST 3.0";

    @Override
    public ProtocolDescription getDescription() {
        return new ProtocolDescription(PROTOCOL_NAME);
    }

    @Override
    public RESTMethodExecutor getExecutor(ServletProtocolConfiguration protocolConfiguration, ProtocolMetaData metaData,
            CommandCallback callback) {
        Collection<HTTPContext> contexts = metaData.getContexts(HTTPContext.class);
        if (contexts.size() == 0) {
            throw new IllegalArgumentException(
                "No " + HTTPContext.class.getName() + " found in " + ProtocolMetaData.class.getName() + ". " +
                    "REST protocol can not be used");
        }
        return new RESTMethodExecutor(protocolConfiguration, contexts, callback);
    }

    @Override
    public DeploymentPackager getPackager() {
        return new RESTDeploymentPackager();
    }

    @Override
    public Class<ServletProtocolConfiguration> getProtocolConfigurationClass() {
        return ServletProtocolConfiguration.class;
    }
}
