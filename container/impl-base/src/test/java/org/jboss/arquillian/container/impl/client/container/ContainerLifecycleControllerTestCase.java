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
package org.jboss.arquillian.container.impl.client.container;

import java.util.List;
import junit.framework.Assert;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.impl.LocalContainerRegistry;
import org.jboss.arquillian.container.impl.client.ContainerDeploymentContextHandler;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartClassContainers;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StartSuiteContainers;
import org.jboss.arquillian.container.spi.event.StopClassContainers;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.StopSuiteContainers;
import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.container.spi.event.container.BeforeSetup;
import org.jboss.arquillian.container.spi.event.container.BeforeStart;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ContainerLifecycleControllerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class ContainerLifecycleControllerTestCase extends AbstractContainerTestBase {
    private static final String CONTAINER_1_NAME = "container_1_suite";
    private static final String CONTAINER_2_NAME = "container_2_suite";
    private static final String CONTAINER_3_NAME = "container_3_class";
    private static final String CONTAINER_4_NAME = "container_4_class";
    private static final String CONTAINER_5_NAME = "container_5_custom";

    @Inject
    private Instance<Injector> injector;

    private ContainerRegistry registry;

    @Mock
    private ServiceLoader serviceLoader;

    @Mock
    private ContainerDef container1;

    @Mock
    private ContainerDef container2;

    @Mock
    private ContainerDef container3;

    @Mock
    private ContainerDef container4;

    @Mock
    private ContainerDef container5;

    @Mock
    private DeployableContainer deployableContainer;

    @Before
    public void setup() {
        when(deployableContainer.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
        when(serviceLoader.onlyOne(eq(DeployableContainer.class))).thenReturn(deployableContainer);
        when(container1.getContainerName()).thenReturn(CONTAINER_1_NAME);
        when(container2.getContainerName()).thenReturn(CONTAINER_2_NAME);
        when(container3.getContainerName()).thenReturn(CONTAINER_3_NAME);
        when(container4.getContainerName()).thenReturn(CONTAINER_4_NAME);
        when(container5.getContainerName()).thenReturn(CONTAINER_5_NAME);
        when(container1.getMode()).thenReturn("suite");
        when(container2.getMode()).thenReturn("suite");
        when(container3.getMode()).thenReturn("class");
        when(container4.getMode()).thenReturn("class");
        when(container5.getMode()).thenReturn("custom");

        registry = new LocalContainerRegistry(injector.get());

        bind(ApplicationScoped.class, ContainerRegistry.class, registry);
    }

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ContainerLifecycleController.class);
        extensions.add(ContainerDeploymentContextHandler.class);
    }

    @Test
    public void shouldSetupAllContainersInRegistry() throws Exception {
        registry.create(container1, serviceLoader);
        registry.create(container2, serviceLoader);
        registry.create(container3, serviceLoader);
        registry.create(container4, serviceLoader);
        registry.create(container5, serviceLoader);

        fire(new SetupContainers());

        assertEventFiredInContext(SetupContainer.class, ContainerContext.class);
        assertEventFired(SetupContainer.class, 5);

        assertEventFiredInContext(BeforeSetup.class, ContainerContext.class);
        assertEventFired(BeforeSetup.class, 5);

        assertEventFiredInContext(AfterSetup.class, ContainerContext.class);
        assertEventFired(AfterSetup.class, 5);

        verify(deployableContainer, times(5)).setup(isA(DummyContainerConfiguration.class));
    }

    @Test
    public void shouldStartAllSuiteContainersInRegistry() throws Exception {
        registry.create(container1, serviceLoader);
        registry.create(container2, serviceLoader);
        registry.create(container3, serviceLoader);
        registry.create(container4, serviceLoader);
        registry.create(container5, serviceLoader);

        fire(new StartSuiteContainers());

        assertEventFiredInContext(StartContainer.class, ContainerContext.class);
        assertEventFired(StartContainer.class, 2);

        assertEventFiredInContext(BeforeStart.class, ContainerContext.class);
        assertEventFired(BeforeStart.class, 2);

        assertEventFiredInContext(AfterStart.class, ContainerContext.class);
        assertEventFired(AfterStart.class, 2);

        verify(deployableContainer, times(2)).start();
    }

    @Test
    public void shouldStartAllClassContainersInRegistry() throws Exception {
        registry.create(container1, serviceLoader);
        registry.create(container2, serviceLoader);
        registry.create(container3, serviceLoader);
        registry.create(container4, serviceLoader);
        registry.create(container5, serviceLoader);

        fire(new StartClassContainers());

        assertEventFiredInContext(StartContainer.class, ContainerContext.class);
        assertEventFired(StartContainer.class, 2);

        assertEventFiredInContext(BeforeStart.class, ContainerContext.class);
        assertEventFired(BeforeStart.class, 2);

        assertEventFiredInContext(AfterStart.class, ContainerContext.class);
        assertEventFired(AfterStart.class, 2);

        verify(deployableContainer, times(2)).start();
    }

    @Test
    public void shouldStopAllSuiteContainersInRegistry() throws Exception {
        registry.create(container1, serviceLoader);
        registry.create(container2, serviceLoader);
        registry.create(container5, serviceLoader);

        //we need to manually set this since we don't actually start them
        for (Container c : registry.getContainers()) {
            c.setState(Container.State.STARTED);
        }

        fire(new StopSuiteContainers());

        assertEventFiredInContext(StopContainer.class, ContainerContext.class);
        assertEventFired(StopContainer.class, 2);

        assertEventFiredInContext(BeforeStop.class, ContainerContext.class);
        assertEventFired(BeforeStop.class, 2);

        assertEventFiredInContext(AfterStop.class, ContainerContext.class);
        assertEventFired(AfterStop.class, 2);

        verify(deployableContainer, times(2)).stop();
    }

    @Test
    public void shouldStopAllClassContainersInRegistry() throws Exception {
        registry.create(container1, serviceLoader);
        registry.create(container2, serviceLoader);
        registry.create(container3, serviceLoader);
        registry.create(container4, serviceLoader);
        registry.create(container5, serviceLoader);

        //we need to manually set this since we don't actually start them
        for (Container c : registry.getContainers()) {
            c.setState(Container.State.STARTED);
        }

        fire(new StopClassContainers());

        assertEventFiredInContext(StopContainer.class, ContainerContext.class);
        assertEventFired(StopContainer.class, 2);

        assertEventFiredInContext(BeforeStop.class, ContainerContext.class);
        assertEventFired(BeforeStop.class, 2);

        assertEventFiredInContext(AfterStop.class, ContainerContext.class);
        assertEventFired(AfterStop.class, 2);

        verify(deployableContainer, times(2)).stop();
    }

    @Test
    public void shouldNotStartCustomContainersInRegistry() throws Exception {
        registry.create(container5, serviceLoader).setState(Container.State.STOPPED);

        fire(new SetupContainers());
        verify(deployableContainer, times(1)).setup(isA(DummyContainerConfiguration.class));

        fire(new StartSuiteContainers());
        verify(deployableContainer, times(0)).start();
        fire(new StartClassContainers());
        verify(deployableContainer, times(0)).start();

        Assert.assertEquals(Container.State.SETUP, registry.getContainer(CONTAINER_5_NAME).getState());
    }

    @Test
    public void shouldNotStopCustomContainersInRegistry() throws Exception {
        registry.create(container5, serviceLoader).setState(Container.State.STARTED);

        fire(new StopClassContainers());
        verify(deployableContainer, times(0)).stop();
        fire(new StopSuiteContainers());
        verify(deployableContainer, times(0)).stop();

        Assert.assertEquals(Container.State.STARTED, registry.getContainer(CONTAINER_5_NAME).getState());
    }

    public static class DummyContainerConfiguration implements ContainerConfiguration {
        @Override
        public void validate() throws ConfigurationException {
        }
    }
}
