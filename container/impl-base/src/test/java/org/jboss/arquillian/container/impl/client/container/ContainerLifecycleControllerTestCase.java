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

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.impl.LocalContainerRegistry;
import org.jboss.arquillian.container.impl.client.ContainerDeploymentContextHandler;
import org.jboss.arquillian.container.impl.client.container.ContainerLifecycleController;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StartSuiteContainers;
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


/**
 * ContainerLifecycleControllerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class ContainerLifecycleControllerTestCase extends AbstractContainerTestBase
{
   private static final String CONTAINER_1_NAME = "container_1";
   private static final String CONTAINER_2_NAME = "container_2";
   
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
   private DeployableContainer deployableContainer;

   @Before
   public void setup() 
   {
      when(deployableContainer.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
      when(serviceLoader.onlyOne(eq(DeployableContainer.class))).thenReturn(deployableContainer);
      when(container1.getContainerName()).thenReturn(CONTAINER_1_NAME);
      when(container2.getContainerName()).thenReturn(CONTAINER_2_NAME);
      when(container1.getMode()).thenReturn("suite");
      when(container2.getMode()).thenReturn("suite");
      
      registry = new LocalContainerRegistry(injector.get());
      
      bind(ApplicationScoped.class, ContainerRegistry.class, registry);
   }
   
   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(ContainerLifecycleController.class);
      extensions.add(ContainerDeploymentContextHandler.class);
   }

   @Test
   public void shouldSetupAllContainersInRegistry() throws Exception
   {
      registry.create(container1, serviceLoader);
      registry.create(container2, serviceLoader);
      
      fire(new SetupContainers());
      
      assertEventFiredInContext(SetupContainer.class, ContainerContext.class);
      assertEventFired(SetupContainer.class, 2);
      
      assertEventFiredInContext(BeforeSetup.class, ContainerContext.class);
      assertEventFired(BeforeSetup.class, 2);
      
      assertEventFiredInContext(AfterSetup.class, ContainerContext.class);
      assertEventFired(AfterSetup.class, 2);
      
      verify(deployableContainer, times(2)).setup(isA(DummyContainerConfiguration.class));
   }

   @Test
   public void shouldStartAllContainersInRegistry() throws Exception
   {
      registry.create(container1, serviceLoader);
      registry.create(container2, serviceLoader);
      
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
   public void shouldStopAllContainersInRegistry() throws Exception
   {
      registry.create(container1, serviceLoader);
      registry.create(container2, serviceLoader);
      
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

   public static class DummyContainerConfiguration implements ContainerConfiguration
   {

      /* (non-Javadoc)
       * @see org.jboss.arquillian.spi.client.container.ContainerConfiguration#validate()
       */
      @Override
      public void validate() throws ConfigurationException
      {
      }
      
   }
}
