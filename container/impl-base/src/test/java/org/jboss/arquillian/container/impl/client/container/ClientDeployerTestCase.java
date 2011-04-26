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

import org.jboss.arquillian.api.Deployer;
import org.jboss.arquillian.container.impl.client.container.ClientDeployerCreator;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.deployment.TargetDescription;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * ClientDeployerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientDeployerTestCase extends AbstractContainerTestBase
{
   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(ClientDeployerCreator.class);
   }

   private static final String DEPLOYMENT_NAME = "DEPLOYMENT";
   
   @Inject
   private Instance<Deployer> deployer;
   
   @Inject
   private Instance<DeploymentScenario> scenario;

   @Before
   public void createSetup()
   {
      ContainerRegistry reg = Mockito.mock(ContainerRegistry.class);
      Container container = Mockito.mock(Container.class);
      Mockito.when(container.getName()).thenReturn("_DEFAULT_");
      
      Mockito.when(reg.getContainer("_DEFAULT_")).thenReturn(container);
      Mockito.when(reg.getContainer(new TargetDescription("_DEFAULT_"))).thenReturn(container);
      
      bind(ApplicationScoped.class, DeploymentScenario.class, new DeploymentScenario());
      bind(ApplicationScoped.class, ContainerRegistry.class, reg);

      fire(new SetupContainers()); // binds the Deployer
   }

   @Test
   public void shouldFireDeploymentEventOnDeploy() throws Exception
   {
      DeploymentDescription description = new DeploymentDescription(DEPLOYMENT_NAME, ShrinkWrap.create(JavaArchive.class));
      scenario.get().addDeployment(description);
      
      deployer.get().deploy(DEPLOYMENT_NAME);
      
      assertEventFired(DeployDeployment.class, 1);
   }

   @Test
   public void shouldFireUnDeploymentEventOnUnDeploy() throws Exception
   {
      DeploymentDescription description = new DeploymentDescription(DEPLOYMENT_NAME, ShrinkWrap.create(JavaArchive.class));
      scenario.get().addDeployment(description);
      
      deployer.get().undeploy(DEPLOYMENT_NAME);
      
      assertEventFired(UnDeployDeployment.class, 1);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeployWhenNotFound() throws Exception
   {
      deployer.get().deploy("UNKNOWN_DEPLOYMENT");
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnUnDeployWhenNotFound() throws Exception
   {
      deployer.get().undeploy("UNKNOWN_DEPLOYMENT");
   }
}
