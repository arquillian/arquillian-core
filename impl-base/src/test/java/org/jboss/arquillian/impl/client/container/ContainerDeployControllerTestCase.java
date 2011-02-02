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

import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.client.container.ContainerLifecycleControllerTestCase.DummyContainerConfiguration;
import org.jboss.arquillian.impl.client.container.event.DeployDeployment;
import org.jboss.arquillian.impl.client.container.event.DeployManagedDeployments;
import org.jboss.arquillian.impl.client.container.event.UnDeployDeployment;
import org.jboss.arquillian.impl.client.container.event.UnDeployManagedDeployments;
import org.jboss.arquillian.impl.configuration.api.ContainerDef;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.ManagerImpl;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.jboss.arquillian.spi.event.container.AfterDeploy;
import org.jboss.arquillian.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.spi.event.container.BeforeUnDeploy;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * ContainerDeployControllerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class ContainerDeployControllerTestCase extends AbstractManagerTestBase
{
   {
      ManagerImpl.DEBUG = true;
   }
   
   private static final String CONTAINER_1_NAME = "container_1";
   private static final String CONTAINER_2_NAME = "container_2";

   private static final String DEPLOYMENT_1_NAME = "deployment_1";
   private static final String DEPLOYMENT_2_NAME = "deployment_2";
   private static final String DEPLOYMENT_3_NAME = "deployment_3_manual";

   @Mock 
   private ServiceLoader serviceLoader;
   
   @Mock
   private ContainerDef container1;

   @Mock
   private ContainerDef container2;

   @Mock
   private DeployableContainer deployableContainer1;

   @Mock
   private DeployableContainer deployableContainer2;

   @Mock
   private ProtocolMetaData protocolMetaData;

   private ContainerRegistry registry = new ContainerRegistry();
   
   private DeploymentScenario scenario = new DeploymentScenario(RunModeType.IN_CONTAINER);

   @Before
   public void setup() throws Exception
   {
      when(deployableContainer1.deploy(isA(Archive.class))).thenReturn(protocolMetaData);
      when(deployableContainer1.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
      when(deployableContainer2.deploy(isA(Archive.class))).thenReturn(protocolMetaData);
      when(deployableContainer2.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
      when(serviceLoader.onlyOne(isA(ClassLoader.class), eq(DeployableContainer.class))).thenReturn(deployableContainer1, deployableContainer2);
      when(container1.getContainerName()).thenReturn(CONTAINER_1_NAME);
      when(container2.getContainerName()).thenReturn(CONTAINER_2_NAME);
      
      Archive<?> archive = ShrinkWrap.create(JavaArchive.class);
      
      scenario.addDeployment(new DeploymentDescription(DEPLOYMENT_1_NAME, archive).setTarget(new TargetDescription(CONTAINER_1_NAME)));
      
      // should use testable archive
      scenario.addDeployment(
            new DeploymentDescription(DEPLOYMENT_2_NAME, archive)
               .setTarget(new TargetDescription(CONTAINER_2_NAME))
               .setTestableArchive(archive));
      
      // should not be deployed during Managed deployments
      scenario.addDeployment(
            new DeploymentDescription(DEPLOYMENT_3_NAME, archive)
               .setTarget(new TargetDescription(CONTAINER_2_NAME))
               .shouldDeployOnStartup(false));
      
      bind(SuiteScoped.class, ContainerRegistry.class, registry);
      bind(ClassScoped.class, DeploymentScenario.class, scenario);
      
   }

   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extensions(ContainerDeployController.class);
   }
   
   @Test
   public void shouldDeployAllManagedDeployments() throws Exception
   {
      registry.create(container1, serviceLoader);
      registry.create(container2, serviceLoader);

      fire(new DeployManagedDeployments());
      
      assertEventFired(DeployDeployment.class, 2);
      assertEventFiredInContext(DeployDeployment.class, ContainerContext.class);
      assertEventFiredInContext(DeployDeployment.class, DeploymentContext.class);
      
      assertEventFired(BeforeDeploy.class, 2);
      assertEventFiredInContext(BeforeDeploy.class, ContainerContext.class);
      assertEventFiredInContext(BeforeDeploy.class, DeploymentContext.class);

      assertEventFired(AfterDeploy.class, 2);
      assertEventFiredInContext(AfterDeploy.class, ContainerContext.class);
      assertEventFiredInContext(AfterDeploy.class, DeploymentContext.class);

      verify(deployableContainer1, times(1)).deploy(isA(Archive.class));
      verify(deployableContainer2, times(1)).deploy(isA(Archive.class));
   }

   @Test
   public void shouldUnDeployAllManagedDeployments() throws Exception
   {
      registry.create(container1, serviceLoader);
      registry.create(container2, serviceLoader);

      fire(new UnDeployManagedDeployments());
      
      assertEventFired(UnDeployDeployment.class, 2);
      assertEventFiredInContext(UnDeployDeployment.class, ContainerContext.class);
      assertEventFiredInContext(UnDeployDeployment.class, DeploymentContext.class);

      assertEventFired(BeforeUnDeploy.class, 2);
      assertEventFiredInContext(BeforeUnDeploy.class, ContainerContext.class);
      assertEventFiredInContext(BeforeUnDeploy.class, DeploymentContext.class);

      assertEventFired(AfterUnDeploy.class, 2);
      assertEventFiredInContext(AfterUnDeploy.class, ContainerContext.class);
      assertEventFiredInContext(AfterUnDeploy.class, DeploymentContext.class);

      verify(deployableContainer1, times(1)).undeploy(isA(Archive.class));
      verify(deployableContainer2, times(1)).undeploy(isA(Archive.class));
   }
   
   @Test
   public void shouldCatchExceptionInDeploymentContext() throws Exception
   {
      registry.create(container1, serviceLoader);
      registry.create(container2, serviceLoader);
      
      when(deployableContainer1.deploy(isA(Archive.class))).thenThrow(new DeploymentException("_TEST_"));
      
      try
      {
         fire(new DeployManagedDeployments());
      }
      catch (Exception e) 
      {
         if(!(e instanceof DeploymentException))
         {
            throw e;
         }
      }
      assertEventFired(DeploymentException.class, 1);
      assertEventFiredInContext(DeploymentException.class, ContainerContext.class);
      assertEventFiredInContext(DeploymentException.class, DeploymentContext.class);
   }
}
