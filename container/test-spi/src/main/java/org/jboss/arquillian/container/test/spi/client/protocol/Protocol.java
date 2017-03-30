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
package org.jboss.arquillian.container.test.spi.client.protocol;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;

/**
 * Interface that defines a Arquillian Protocol.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface Protocol<T extends ProtocolConfiguration> {
    /**
     * Get the protocols configuration class. <br/>
     * <p>
     * A instance of this class will be filled with the configuration data
     * configured in e.g. arquillian.xml and passed back to
     * {@link #getExecutor(ProtocolConfiguration, ProtocolMetaData)}
     *
     * @return The type used for configuration
     *
     * @see ProtocolConfiguration
     */
    Class<T> getProtocolConfigurationClass();

    /**
     * The registry name used for this protocol.<br/>
     * e.g. EJB, Servlet-2.5, Servlet-3.0 <br/>
     * <br/>
     *
     * @return The protocol name
     *
     * @see org.jboss.arquillian.api.OverProtocol
     */
    ProtocolDescription getDescription();

    /**
     * Get the DeploymentPackager for this Protocol. <br/>
     * Used so the Protocol can bind it self to the users deployment.
     */
    DeploymentPackager getPackager();

    /**
     * @param callback
     *     A Callback for each event received from the remote container.
     */
    ContainerMethodExecutor getExecutor(T protocolConfiguration, ProtocolMetaData metaData, CommandCallback callback);
}
