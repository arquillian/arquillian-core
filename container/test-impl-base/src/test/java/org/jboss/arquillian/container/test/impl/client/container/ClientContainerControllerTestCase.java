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
package org.jboss.arquillian.container.test.impl.client.container;

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentTargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.KillContainer;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * ClientContainerControllerTestCase
 *
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientContainerControllerTestCase extends AbstractContainerTestTestBase
{
   private static final String MANAGED_SERVER_NAME = "suiteOrClassServer";
   private static final String MANUAL_SERVER_NAME = "manualServer";
   private static final String UNKNOWN_SERVER = "unknown";
   
   private static final String DEPLOYMENT_NAME = "DEPLOYMENT";
   
   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(ClientContainerControllerCreator.class);
   }
   
   @Inject
   private Instance<ContainerController> controller;
   
   @Inject
   private Instance<DeploymentScenario> scenario;
   
   @Before
   public void createSetup()
   {
      ContainerRegistry reg = Mockito.mock(ContainerRegistry.class);
      Container containerManaged = Mockito.mock(Container.class);
      ContainerDef suiteContainerDef = Mockito.mock(ContainerDef.class);
      Mockito.when(suiteContainerDef.getMode()).thenReturn("suite");
      Mockito.when(containerManaged.getContainerConfiguration()).thenReturn(suiteContainerDef);
      Mockito.when(containerManaged.getName()).thenReturn(MANAGED_SERVER_NAME);
      Container containerManual = Mockito.mock(Container.class);
      ContainerDef manualContainerDef = Mockito.mock(ContainerDef.class);
      Mockito.when(manualContainerDef.getMode()).thenReturn("manual");
      Mockito.when(containerManual.getContainerConfiguration()).thenReturn(manualContainerDef);
      Mockito.when(containerManual.getName()).thenReturn(MANUAL_SERVER_NAME);
      
      List<Container> containers = new ArrayList<Container>();
      containers.add(containerManaged);
      containers.add(containerManual);
      Mockito.when(reg.getContainers()).thenReturn(containers);
      Mockito.when(reg.getContainer(new TargetDescription(MANAGED_SERVER_NAME))).thenReturn(containerManaged);
      Mockito.when(reg.getContainer(new TargetDescription(MANUAL_SERVER_NAME))).thenReturn(containerManual);
      
      bind(ApplicationScoped.class, ContainerRegistry.class, reg);
      bind(ApplicationScoped.class, DeploymentScenario.class, new DeploymentScenario());
      
      fire(new SetupContainers());
   }

   @Test
   public void shouldFireStartContainerEventOnStart() throws Exception
   {
      controller.get().start(MANUAL_SERVER_NAME);

      assertEventFired(StartContainer.class, 1);
   }
   
   @Test
   public void shouldFireDeployDeploymentEventOnStartWhenManagedDeployment() throws Exception
   {
      DeploymentDescription description = new DeploymentDescription(DEPLOYMENT_NAME, ShrinkWrap.create(JavaArchive.class));
      description.shouldBeManaged(true);
      description.setTarget(new TargetDescription(MANUAL_SERVER_NAME));
      scenario.get().addDeployment(description);
      
      controller.get().start(MANUAL_SERVER_NAME);
      controller.get().start(MANUAL_SERVER_NAME, new Config().add("managementPort", "19999").map());

      assertEventFired(DeployDeployment.class, 2);
   }
   
   @Test
   public void shouldNotFireDeployDeploymentEventOnStartWhenNotManagedDeployment() throws Exception
   {
      DeploymentDescription description = new DeploymentDescription(DEPLOYMENT_NAME, ShrinkWrap.create(JavaArchive.class));
      description.shouldBeManaged(false);
      description.setTarget(new TargetDescription(MANUAL_SERVER_NAME));
      scenario.get().addDeployment(description);
      
      controller.get().start(MANUAL_SERVER_NAME);
      controller.get().start(MANUAL_SERVER_NAME, new Config().add("managementPort", "19999").map());
      
      assertEventFired(DeployDeployment.class, 0);
   }
   
   @Test
   public void shouldFireUnDeployDeploymentEventOnStopWhenManagedDeployment() throws Exception
   {
      DeploymentDescription description = new DeploymentDescription(DEPLOYMENT_NAME, ShrinkWrap.create(JavaArchive.class));
      description.shouldBeManaged(true);
      description.setTarget(new TargetDescription(MANUAL_SERVER_NAME));
      scenario.get().addDeployment(description);
      controller.get().start(MANUAL_SERVER_NAME);
      scenario.get().deployment(new DeploymentTargetDescription(DEPLOYMENT_NAME)).deployed();
      
      controller.get().stop(MANUAL_SERVER_NAME);
      
      assertEventFired(UnDeployDeployment.class, 1);
   }
   
   @Test
   public void shouldNotFireUnDeployDeploymentEventOnStopWhenNotManagedDeployment() throws Exception
   {
      DeploymentDescription description = new DeploymentDescription(DEPLOYMENT_NAME, ShrinkWrap.create(JavaArchive.class));
      description.shouldBeManaged(false);
      description.setTarget(new TargetDescription(MANUAL_SERVER_NAME));
      scenario.get().addDeployment(description);
      controller.get().start(MANUAL_SERVER_NAME);
      scenario.get().deployment(new DeploymentTargetDescription(DEPLOYMENT_NAME)).deployed();
      
      controller.get().stop(MANUAL_SERVER_NAME);
      
      assertEventFired(UnDeployDeployment.class, 0);
   }
   
   @Test
   public void shouldFireStartContainerEventOnStartWithOverrides() throws Exception
   {
      controller.get().start(MANUAL_SERVER_NAME, new Config().add("managementPort", "19999").map());
      
      assertEventFired(StartContainer.class, 1);
   }

   @Test
   public void shouldFireStopContainerEventOnStop() throws Exception
   {
      controller.get().stop(MANUAL_SERVER_NAME);
      
      assertEventFired(StopContainer.class, 1);
   }
   
   @Test
   public void shouldFireKillContainerEventOnKill() throws Exception
   {
      controller.get().kill(MANUAL_SERVER_NAME);
      
      assertEventFired(KillContainer.class, 1);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnStartWhenManaged() throws Exception
   {
      controller.get().start(MANAGED_SERVER_NAME);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnStartWithOverridesWhenManaged() throws Exception
   {
      controller.get().start(MANAGED_SERVER_NAME, new Config().add("managementPort", "19999").map());
   }
      
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnStopWhenManaged() throws Exception
   {
      controller.get().stop(MANAGED_SERVER_NAME);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnKillWhenManaged() throws Exception
   {
      controller.get().kill(MANAGED_SERVER_NAME);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnStartWhenNotFound() throws Exception
   {
      controller.get().start(UNKNOWN_SERVER);
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnStopWhenNotFound() throws Exception
   {
      controller.get().stop(UNKNOWN_SERVER);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnKillWhenNotFound() throws Exception
   {
      controller.get().kill(UNKNOWN_SERVER);
   }
}
