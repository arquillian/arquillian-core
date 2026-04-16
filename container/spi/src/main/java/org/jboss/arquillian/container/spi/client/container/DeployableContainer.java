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
 * <p>
 * Methods to get the configuration class, the default protocol and to deploy
 * and undeploy an archive are required to be implemented. Other
 * methods such as setup, start and stop default to NOOP, as not every
 * type of container needs setup or an explicit start and stop.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 */
public interface DeployableContainer<T extends ContainerConfiguration> {

    /**
     * Returns the configuration class for this container.
     * <p>
     * This method provides the Class object for the container-specific configuration type.
     * The configuration class is used by Arquillian to create and populate a configuration instance
     * from the values defined in arquillian.xml or other configured descriptors.
     * <p>
     * Container implementations must implement this method to specify their configuration class,
     * which must extend {@link ContainerConfiguration}.
     *
     * @return the Class object representing the container's configuration type
     */
    Class<T> getConfigurationClass();

    /**
     * Provide a mapping instance that takes the {@link org.jboss.arquillian.config.descriptor.api.ContainerDef}
     * for the arquillian.xml or other configured descriptor and populates the container configuration
     * instance from the descriptor values.
     *
     * @return A possibly null {@link ConfigurationMapper}. If {@literal null}, the default logic to map from string based
     * properties as implemented in {@link org.jboss.arquillian.container.impl.MapObject} will be used.
     */
    default ConfigurationMapper<T> getConfigurationMapper() {
        return null;
    }

    /**
     * Sets up the container with the provided configuration.
     * <p>
     * This method is called before {@link #start()} to initialize the container with its configuration.
     * Container implementations should use this method to perform any necessary setup operations such as
     * validating configuration, initializing resources, or preparing the container runtime environment.
     * <p>
     * The default implementation is a no-op. Container implementations that do not require setup
     * do not need to override this method.
     *
     * @param configuration the container configuration instance populated with values from the arquillian.xml
     *                     or other configured descriptor
     */
    default void setup(T configuration) {
        // The default implementation is a no-op.
    }

    /**
     * Starts the container.
     * <p>
     * This method is called to start the container after it has been set up via {@link #setup(Object)}.
     * Container implementations should use this method to start the container runtime, such as launching
     * a server process, connecting to a running container, or initializing container services.
     * <p>
     * The default implementation is a no-op. Container implementations that do not require an explicit
     * start operation (e.g., containers that are already running) do not need to override this method.
     *
     * @throws LifecycleException if the container fails to start
     */
    default void start() throws LifecycleException {
        // The default implementation is a no-op.
    }

    /**
     * Stops the container.
     * <p>
     * This method is called to stop a running container. Container implementations should use this method
     * to gracefully shut down the container runtime, such as stopping a server process, disconnecting from
     * a running container, or releasing container resources.
     * <p>
     * The default implementation is a no-op. Container implementations that do not require an explicit
     * stop operation (e.g., containers managed externally) do not need to override this method.
     *
     * @throws LifecycleException if the container fails to stop
     */
    default void stop() throws LifecycleException {
        // The default implementation is a no-op.
    }

    /**
     * Returns the default protocol for this container.
     * <p>
     * The protocol describes how Arquillian communicates with the container to execute tests.
     * This includes information about the protocol type (e.g., Servlet, JAX-RS, EJB) and version.
     * The protocol is used to determine how to package test enrichers and how to invoke test methods
     * inside the container.
     * <p>
     * Container implementations must implement this method to specify their default communication protocol.
     * If the container supports multiple protocols, this method should return the most commonly used or
     * preferred protocol. Users can override the default protocol in their arquillian.xml configuration.
     *
     * @return the default protocol description for this container, must not be null
     */
    ProtocolDescription getDefaultProtocol();

    /**
     * Deploys an archive to the container.
     * <p>
     * This method deploys the provided archive (e.g., WAR, EAR, JAR) to the container and returns
     * metadata about the deployment. The returned {@link ProtocolMetaData} contains information needed
     * by Arquillian to communicate with the deployed application, such as servlet context information,
     * HTTP endpoints, or other protocol-specific details.
     * <p>
     * Container implementations must implement this method to handle archive deployments.
     * The implementation should:
     * <ul>
     *   <li>Deploy the archive to the container runtime</li>
     *   <li>Wait for the deployment to complete</li>
     *   <li>Gather and return protocol metadata for test execution</li>
     * </ul>
     *
     * @param archive the archive to deploy, must not be null
     * @return protocol metadata describing how to communicate with the deployed archive, must not be null
     * @throws DeploymentException if the deployment fails for any reason
     */
    ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException;

    /**
     * Undeploys an archive from the container.
     * <p>
     * This method removes a previously deployed archive from the container. It should perform cleanup
     * and ensure the archive is completely removed from the container runtime.
     * <p>
     * Container implementations must implement this method to handle archive undeployment.
     * The implementation should:
     * <ul>
     *   <li>Undeploy the archive from the container runtime</li>
     *   <li>Wait for the undeployment to complete</li>
     *   <li>Clean up any resources associated with the deployment</li>
     * </ul>
     * <p>
     * This method is called even if the deployment failed or encountered errors, so implementations
     * should handle cleanup gracefully.
     *
     * @param archive the archive to undeploy, must not be null
     * @throws DeploymentException if the undeployment fails for any reason
     */
    void undeploy(Archive<?> archive) throws DeploymentException;

    /**
     * Deploys a descriptor to the container.
     * <p>
     * <strong>This method is deprecated for removal and will be removed in a future version.</strong>
     * Descriptor-based deployments are not widely supported by container implementations
     * and this functionality may be removed. Archive-based deployments via
     * {@link #deploy(Archive)} should be used instead.
     * <p>
     * The default implementation is a no-op and does not throw an exception.
     * Container implementations that do not support descriptor deployments do not need to override this method.
     * <p>
     * Note that ideally, this method would throw {@link UnsupportedOperationException}
     * by default to clearly indicate lack of support. However, this would be a breaking change for existing
     * container implementations and would require all implementations to override the method (and thus add
     * a dependency on shrinkwrap-descriptors) even if they don't support descriptor deployments.
     * The no-op default was chosen to maintain backwards compatibility and avoid forcing unnecessary
     * dependencies on container implementations.
     *
     * @param descriptor the descriptor to deploy
     * @throws DeploymentException if deployment fails
     * @deprecated for removal. This method will be removed in a future version. Use {@link #deploy(Archive)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.10.1.Final")
    default void deploy(Descriptor descriptor) throws DeploymentException {
        // The default implementation is a no-op.
    }

    /**
     * Undeploys a descriptor from the container.
     * <p>
     * <strong>This method is deprecated for removal and will be removed in a future version.</strong>
     * Descriptor-based deployments are not widely supported by container implementations
     * and this functionality may be removed. Archive-based deployments via
     * {@link #undeploy(Archive)} should be used instead.
     * <p>
     * The default implementation is a no-op and does not throw an exception.
     * Container implementations that do not support descriptor deployments do not need to override this method.
     * <p>
     * Note that ideally, this method would throw {@link UnsupportedOperationException}
     * by default to clearly indicate lack of support. However, this would be a breaking change for existing
     * container implementations and would require all implementations to override the method (and thus add
     * a dependency on shrinkwrap-descriptors) even if they don't support descriptor deployments.
     * The no-op default was chosen to maintain backwards compatibility and avoid forcing unnecessary
     * dependencies on container implementations.
     *
     * @param descriptor the descriptor to undeploy
     * @throws DeploymentException if undeployment fails
     * @deprecated for removal. This method will be removed in a future version. Use {@link #undeploy(Archive)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.10.1.Final")
    default void undeploy(Descriptor descriptor) throws DeploymentException {
        // The default implementation is a no-op.
    }
}
