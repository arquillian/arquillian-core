/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.impl.client.container;

import java.util.Arrays;

import junit.framework.Assert;

import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.event.container.AfterDeploy;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * DeploymentHandlerTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerDeployerTestCase extends AbstractManagerTestBase
{
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(ContainerDeployer.class);
   }
   
   @Test(expected = IllegalStateException.class)
   @Ignore // no exception handling
   public void shouldThrowIllegalStateOnMissingDeployableContainer() throws Exception 
   {
   }

   @Test(expected = IllegalStateException.class)
   @Ignore // no exception handling
   public void shouldThrowIllegalStateOnMissingArchive() throws Exception 
   {
   }

   @Test
   public void shouldExportContainerMethodExecutorAndFireEvents() throws Exception 
   {
      DeploymentScenario scenario = new DeploymentScenario(RunModeType.IN_CONTAINER);
      scenario.addDeployment(new DeploymentDescription("_TEST_", ShrinkWrap.create(JavaArchive.class, "test.jar")));
      bind(ClassScoped.class, DeploymentScenario.class, scenario);
      
      Container container = Mockito.mock(Container.class);
      final DeployableContainer<ContainerConfiguration> deployableContainer = Mockito.mock(DeployableContainer.class);
      Mockito.when(deployableContainer.deploy(Mockito.isA(Deployment.class))).thenReturn(new ProtocolMetaData());
      
      ContainerRegistry registry = Mockito.mock(ContainerRegistry.class);
      bind(ApplicationScoped.class, ContainerRegistry.class, registry);

      Mockito.when(container.getName()).thenReturn("_TEST_");
      Mockito.when(container.getDeployableContainer()).thenAnswer(new Answer<DeployableContainer<?>>()
      {
         @Override
         public DeployableContainer<?> answer(InvocationOnMock invocation) throws Throwable
         {
            return deployableContainer;
         }
      });
      Mockito.when(registry.getContainers()).thenReturn(Arrays.asList(container));
      Mockito.when(registry.getContainer(TargetDescription.DEFAULT)).thenReturn(container);

      fire(new BeforeClass(ContainerDeployerTestCase.class));
      
      // verify that the deployment was deployed to the container
      Mockito.verify(deployableContainer).deploy(Mockito.any(Deployment.class));
      
      // verify that the ContainerMethodExecutor was exported
      ContainerContext context = getManager().getContext(ContainerContext.class);
      context.activate(container.getName());
      
      ProtocolMetaData metadata = context.getObjectStore().get(ProtocolMetaData.class);
      
      Assert.assertNotNull(
            "Should have exported " + ProtocolMetaData.class.getSimpleName(), 
            metadata);
      
      // verify that all the events where fired
      assertEventFired(BeforeDeploy.class, 1);
      assertEventFiredInContext(BeforeDeploy.class, ContainerContext.class);
      assertEventFired(AfterDeploy.class, 1);
      assertEventFiredInContext(AfterDeploy.class, ContainerContext.class);
   }
}
