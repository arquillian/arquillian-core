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
package org.jboss.arquillian.container.test.impl.execution;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.container.test.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.container.test.impl.execution.event.RemoteExecutionEvent;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.client.protocol.ProtocolConfiguration;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.threading.ContextSnapshot;
import org.jboss.arquillian.core.api.threading.ExecutorService;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.annotation.TestScoped;

/**
 * A Handler for executing the remote Test Method.<br/>
 * <br/>
 * <b>Imports:</b><br/>
 * {@link ProtocolMetaData}<br/>
 * {@link DeploymentScenario}<br/>
 * {@link ContainerRegistry}<br/>
 * {@link ProtocolRegistry}<br/>
 * <br/>
 * <b>Exports:</b><br/>
 * {@link TestResult}<br/>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * @see DeployableContainer
 */
public class RemoteTestExecuter {
    @Inject
    private Instance<DeploymentDescription> deployment;

    @Inject
    private Instance<Container> container;

    @Inject
    private Instance<ProtocolRegistry> protocolRegistry;

    @Inject
    private Instance<ProtocolMetaData> protocolMetadata;

    @Inject
    private Event<Object> remoteEvent;

    @Inject
    @TestScoped
    private InstanceProducer<TestResult> testResult;

    @Inject
    private Instance<ExecutorService> executorService;

    public void execute(@Observes RemoteExecutionEvent event) throws Exception {
        Container container = this.container.get();
        DeploymentDescription deployment = this.deployment.get();

        ProtocolRegistry protoReg = protocolRegistry.get();

        // if no default marked or specific protocol defined in the registry, use the DeployableContainers defaultProtocol.
        ProtocolDefinition protocol = protoReg.getProtocol(deployment.getProtocol());
        if (protocol == null) {
            protocol = protoReg.getProtocol(container.getDeployableContainer().getDefaultProtocol());
        }

        ProtocolConfiguration protocolConfiguration;

        if (container.hasProtocolConfiguration(protocol.getProtocolDescription())) {
            protocolConfiguration = protocol.createProtocolConfiguration(
                container.getProtocolConfiguration(protocol.getProtocolDescription()).getProtocolProperties());
        } else {
            protocolConfiguration = protocol.createProtocolConfiguration();
        }
        ContainerMethodExecutor executor = getContainerMethodExecutor(protocol, protocolConfiguration);
        testResult.set(executor.invoke(event.getExecutor()));
    }

    // TODO: cast to raw type to get away from generic issue..
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ContainerMethodExecutor getContainerMethodExecutor(ProtocolDefinition protocol,
        ProtocolConfiguration protocolConfiguration) {
        final ContextSnapshot state = executorService.get().createSnapshotContext();

        ContainerMethodExecutor executor = ((Protocol) protocol.getProtocol()).getExecutor(
            protocolConfiguration,
            protocolMetadata.get(), new CommandCallback() {
                @Override
                public void fired(Command<?> event) {
                    state.activate();
                    try {
                        remoteEvent.fire(event);
                    } finally {
                        state.deactivate();
                    }
                }
            });
        return executor;
    }
}
