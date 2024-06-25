/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.spi.client.container;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * This interface defines a DeployableContainer in Arquillian.
 *
 * <p>
 * Methods to get the configuration class, the default protocol and to deploy
 * and undeploy an archive are required to be implemented. Other
 * methods such as setup, start and stop default to NOOP, as not every
 * type of container needs setup or an explicit start and stop.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface DeployableContainer<T extends ContainerConfiguration> {
    // ControllableContainer
    Class<T> getConfigurationClass();

    /**
     * Provide a mapping instance that takes the {@link org.jboss.arquillian.config.descriptor.api.ContainerDef}
     * for the arquillian.xml or other configured descriptor and populates the container configuration
     * instance from the descriptor values.
     *
     * @return A possibly null ConfigurationMapper. If null, the default logic to map from string based
     * properties as implemented in org.jboss.arquillian.container.impl.MapObject will be used.
     */
    default ConfigurationMapper getConfigurationMapper() {
        return null;
    }
    default void setup(T configuration) {
    }

    default void start() throws LifecycleException {
    }

    default void stop() throws LifecycleException {
    }

    // DeployableContainer
    ProtocolDescription getDefaultProtocol();

    ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException;

    void undeploy(Archive<?> archive) throws DeploymentException;

    // Admin ?
    default void deploy(Descriptor descriptor) throws DeploymentException {
    }

    default void undeploy(Descriptor descriptor) throws DeploymentException {
    }
}
