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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.impl.LocalContainerRegistry;
import org.jboss.arquillian.container.impl.client.ContainerDeploymentContextHandler;
import org.jboss.arquillian.container.impl.client.container.ContainerLifecycleControllerTestCase.DummyContainerConfiguration;
import org.jboss.arquillian.container.spi.Container.State;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentTargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.cdi.beans.BeansDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
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
public class ContainerDeployControllerTestCase extends AbstractContainerTestBase
{
   private static final String CONTAINER_1_NAME = "container_1";
   private static final String CONTAINER_2_NAME = "container_2";

   private static final String DEPLOYMENT_1_NAME = "deployment_1";
   private static final String DEPLOYMENT_2_NAME = "deployment_2";
   private static final String DEPLOYMENT_3_NAME = "deployment_3_manual";
   private static final String DEPLOYMENT_4_NAME = "deployment_4_descriptor";
   
   @Inject
   private Instance<Injector> injector;
   
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

   private ContainerRegistry registry;
   
   private DeploymentScenario scenario = new DeploymentScenario();

   @Before
   public void setup() throws Exception
   {
      when(deployableContainer1.deploy(isA(Archive.class))).thenReturn(protocolMetaData);
      when(deployableContainer1.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
      when(deployableContainer2.deploy(isA(Archive.class))).thenReturn(protocolMetaData);
      when(deployableContainer2.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
      when(serviceLoader.onlyOne(eq(DeployableContainer.class))).thenReturn(deployableContainer1, deployableContainer2);
      when(container1.getContainerName()).thenReturn(CONTAINER_1_NAME);
      when(container2.getContainerName()).thenReturn(CONTAINER_2_NAME);
      
      scenario.addDeployment(
            new DeploymentDescription(DEPLOYMENT_1_NAME, ShrinkWrap.create(JavaArchive.class))
               .setTarget(new TargetDescription(CONTAINER_1_NAME))
               .shouldBeTestable(false)
               .setOrder(2));
      
      // should use testable archive
      scenario.addDeployment(
            new DeploymentDescription(DEPLOYMENT_2_NAME, ShrinkWrap.create(JavaArchive.class))
               .setTarget(new TargetDescription(CONTAINER_2_NAME))
               .setOrder(1)
               .shouldBeTestable(true)
               .setTestableArchive(ShrinkWrap.create(JavaArchive.class)));
      
      // should not be deployed during Managed deployments
      scenario.addDeployment(
            new DeploymentDescription(DEPLOYMENT_3_NAME, ShrinkWrap.create(JavaArchive.class))
               .setTarget(new TargetDescription(CONTAINER_2_NAME))
               .setOrder(3)
               .shouldBeTestable(false)
               .shouldBeManaged(false));
      
      scenario.addDeployment(
            new DeploymentDescription(DEPLOYMENT_4_NAME, Descriptors.create(BeansDescriptor.class))
               .setTarget(new TargetDescription(CONTAINER_1_NAME))
               .setOrder(4)
               .shouldBeManaged(true));
      
      registry = new LocalContainerRegistry(injector.get());
      
      bind(ApplicationScoped.class, ContainerRegistry.class, registry);
      bind(ApplicationScoped.class, DeploymentScenario.class, scenario);
      
   }

   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(ContainerDeployController.class);
      extensions.add(ContainerDeploymentContextHandler.class);
   }
   
   @Test
   public void shouldDeployAllManagedDeployments() throws Exception
   {
      registry.create(container1, serviceLoader);
      registry.create(container2, serviceLoader);

      fire(new DeployManagedDeployments());
      
      assertEventFired(DeployDeployment.class, 3);
      assertEventFiredInContext(DeployDeployment.class, ContainerContext.class);
      assertEventFiredInContext(DeployDeployment.class, DeploymentContext.class);
      
      assertEventFired(BeforeDeploy.class, 3);
      assertEventFiredInContext(BeforeDeploy.class, ContainerContext.class);
      assertEventFiredInContext(BeforeDeploy.class, DeploymentContext.class);

      assertEventFired(AfterDeploy.class, 3);
      assertEventFiredInContext(AfterDeploy.class, ContainerContext.class);
      assertEventFiredInContext(AfterDeploy.class, DeploymentContext.class);

      verify(deployableContainer1, times(1)).deploy(isA(Archive.class));
      verify(deployableContainer1, times(1)).deploy(isA(Descriptor.class));
      verify(deployableContainer2, times(1)).deploy(isA(Archive.class));

      InOrder ordered = inOrder(deployableContainer1, deployableContainer2);
      ordered.verify(deployableContainer2, times(1)).deploy(
            scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_2_NAME)).getDescription().getTestableArchive());
      
      ordered.verify(deployableContainer1, times(1)).deploy(
            scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_1_NAME)).getDescription().getArchive());
      
      ordered.verify(deployableContainer1, times(1)).deploy(
            scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_4_NAME)).getDescription().getDescriptor());
   }

   @Test
   public void shouldUnDeployAllManagedDeployments() throws Exception
   {
      registry.create(container1, serviceLoader);
      registry.create(container2, serviceLoader);
      registry.getContainer(CONTAINER_1_NAME).setState(State.STARTED);
      registry.getContainer(CONTAINER_2_NAME).setState(State.STARTED);
      
      // setup all deployment as deployed so that we can observe UnDeployDeployment events
      scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_1_NAME)).deployed();
      scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_2_NAME)).deployed();
      scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_3_NAME)).deployed();
      scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_4_NAME)).deployed();
      
      fire(new UnDeployManagedDeployments());
      
      assertEventFired(UnDeployDeployment.class, 4);
      assertEventFiredInContext(UnDeployDeployment.class, ContainerContext.class);
      assertEventFiredInContext(UnDeployDeployment.class, DeploymentContext.class);

      assertEventFired(BeforeUnDeploy.class, 4);
      assertEventFiredInContext(BeforeUnDeploy.class, ContainerContext.class);
      assertEventFiredInContext(BeforeUnDeploy.class, DeploymentContext.class);

      assertEventFired(AfterUnDeploy.class, 4);
      assertEventFiredInContext(AfterUnDeploy.class, ContainerContext.class);
      assertEventFiredInContext(AfterUnDeploy.class, DeploymentContext.class);

      verify(deployableContainer1, times(1)).undeploy(isA(Archive.class));
      verify(deployableContainer1, times(1)).undeploy(isA(Descriptor.class));
      verify(deployableContainer2, times(2)).undeploy(isA(Archive.class));
      

      InOrder ordered = inOrder(deployableContainer1, deployableContainer2);
      ordered.verify(deployableContainer1, times(1)).undeploy(
            scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_4_NAME)).getDescription().getDescriptor());

      ordered.verify(deployableContainer2, times(1)).undeploy(
            scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_3_NAME)).getDescription().getArchive());

      ordered.verify(deployableContainer1, times(1)).undeploy(
            scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_1_NAME)).getDescription().getArchive());

      ordered.verify(deployableContainer2, times(1)).undeploy(
            scenario.deployment(new DeploymentTargetDescription(DEPLOYMENT_2_NAME)).getDescription().getTestableArchive());

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
      
      assertEventFiredTyped(Throwable.class, 1);
   }
}
