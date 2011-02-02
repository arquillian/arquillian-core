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
package org.jboss.arquillian.impl.client.container;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.client.container.event.SetupContainer;
import org.jboss.arquillian.impl.client.container.event.SetupContainers;
import org.jboss.arquillian.impl.client.container.event.StartContainer;
import org.jboss.arquillian.impl.client.container.event.StartManagedContainers;
import org.jboss.arquillian.impl.client.container.event.StopContainer;
import org.jboss.arquillian.impl.client.container.event.StopManagedContainers;
import org.jboss.arquillian.impl.configuration.api.ContainerDef;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.jboss.arquillian.spi.event.container.AfterSetup;
import org.jboss.arquillian.spi.event.container.AfterStart;
import org.jboss.arquillian.spi.event.container.AfterStop;
import org.jboss.arquillian.spi.event.container.BeforeSetup;
import org.jboss.arquillian.spi.event.container.BeforeStart;
import org.jboss.arquillian.spi.event.container.BeforeStop;
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
public class ContainerLifecycleControllerTestCase extends AbstractManagerTestBase
{
   private static final String CONTAINER_1_NAME = "container_1";
   private static final String CONTAINER_2_NAME = "container_2";
   
   private ContainerRegistry registry = new ContainerRegistry();
   
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
      when(serviceLoader.onlyOne(isA(ClassLoader.class), eq(DeployableContainer.class))).thenReturn(deployableContainer);
      when(container1.getContainerName()).thenReturn(CONTAINER_1_NAME);
      when(container2.getContainerName()).thenReturn(CONTAINER_2_NAME);
      
      bind(SuiteScoped.class, ContainerRegistry.class, registry);
   }
   
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extensions(ContainerLifecycleController.class);
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
      
      fire(new StartManagedContainers());
      
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
      
      fire(new StopManagedContainers());
      
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
      
   }
}
