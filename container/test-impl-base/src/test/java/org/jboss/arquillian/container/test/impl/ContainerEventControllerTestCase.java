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
package org.jboss.arquillian.container.test.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.impl.LocalContainerRegistry;
import org.jboss.arquillian.container.impl.client.ContainerDeploymentContextHandler;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartSuiteContainers;
import org.jboss.arquillian.container.spi.event.StopSuiteContainers;
import org.jboss.arquillian.container.spi.event.StopManualContainers;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.impl.client.ContainerEventController;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.impl.InjectorImpl;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * ContainerBeforeAfterControllerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerEventControllerTestCase extends AbstractContainerTestTestBase
{
   private static final String CONTAINER_1_NAME = "container_1";

   private static final String DEPLOYMENT_1_NAME = "deployment_1";

   @Inject
   private Instance<Injector> injector;
   
   @Mock 
   private ServiceLoader serviceLoader;

   @Mock
   private ContainerDef container1;

   @SuppressWarnings("rawtypes")
   @Mock
   private DeployableContainer deployableContainer1;

   private ContainerRegistry registry;

   private DeploymentScenario scenario = new DeploymentScenario();

   /* (non-Javadoc)
    * @see org.jboss.arquillian.core.test.AbstractManagerTestBase#addExtensions(java.util.List)
    */
   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(ContainerEventController.class);
      extensions.add(ContainerDeploymentContextHandler.class);
   }

   @Before
   public void scenario() throws Exception
   {
      registry = new LocalContainerRegistry(injector.get());
      when(container1.getContainerName()).thenReturn(CONTAINER_1_NAME);
      //when(injector.get()).thenReturn(InjectorImpl.of(manager));
      when(serviceLoader.onlyOne(eq(DeployableContainer.class))).thenReturn(deployableContainer1);

      Archive<?> archive = ShrinkWrap.create(JavaArchive.class);

      scenario.addDeployment(new DeploymentDescription(DEPLOYMENT_1_NAME, archive).setTarget(new TargetDescription(CONTAINER_1_NAME)));

      registry.create(container1, serviceLoader);

      bind(SuiteScoped.class, ContainerRegistry.class, registry);
      bind(ClassScoped.class, DeploymentScenario.class, scenario);
   }

   @Test
   public void shouldSetupAndStartContainers() throws Exception
   {
      fire(new BeforeSuite());

      assertEventFired(SetupContainers.class, 1);
      assertEventFired(StartSuiteContainers.class, 1);
   }

   @Test
   public void shouldStopContainers() throws Exception
   {
      fire(new AfterSuite());

      assertEventFired(StopSuiteContainers.class, 1);
   }

   @Test
   public void shouldDeployManagedDeployments() throws Exception
   {
      fire(new BeforeClass(testClass()));

      assertEventFired(DeployManagedDeployments.class, 1);
   }

   @Test
   public void shouldUnDeployManagedDeploymentsAndStopManualContainers() throws Exception
   {
      fire(new AfterClass(testClass()));

      assertEventFired(UnDeployManagedDeployments.class, 1);
      assertEventFired(StopManualContainers.class, 1);
   }

   @Test
   public void shouldInvokeBeforeInContainerDeploymentContext() throws Exception
   {
      fire(new org.jboss.arquillian.test.spi.event.suite.Before(this, testMethod()));

      assertEventFiredInContext(org.jboss.arquillian.test.spi.event.suite.Before.class, ContainerContext.class);
      assertEventFiredInContext(org.jboss.arquillian.test.spi.event.suite.Before.class, DeploymentContext.class);
   }

   @Test
   public void shouldInvokeTestInContainerDeploymentContext() throws Exception
   {
      fire(new org.jboss.arquillian.test.spi.event.suite.Test(new TestMethodExecutor()
      {
         @Override
         public void invoke(Object... parameters) throws Throwable { }

         @Override
         public Method getMethod()
         {
            return testMethod();
         }

         @Override
         public Object getInstance()
         {
            return ContainerEventControllerTestCase.this;
         }
      }));

      assertEventFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, ContainerContext.class);
      assertEventFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, DeploymentContext.class);
   }

   @Test
   public void shouldNotInvokeTestInContainerDeploymentContextIfNoDeploymentFound() throws Exception
   {
      // override previous bound DeploymentScenario with a empty set
      bind(ClassScoped.class, DeploymentScenario.class, new DeploymentScenario());
      fire(new org.jboss.arquillian.test.spi.event.suite.Test(new TestMethodExecutor()
      {
         @Override
         public void invoke(Object... parameters) throws Throwable { }

         @Override
         public Method getMethod()
         {
            return testMethod();
         }

         @Override
         public Object getInstance()
         {
            return ContainerEventControllerTestCase.this;
         }
      }));

      assertEventNotFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, ContainerContext.class);
      assertEventNotFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, DeploymentContext.class);
   }

   @Test(expected = IllegalStateException.class)
   public void shouldThrowExceptionIfTryingToOperateOnANonExistingContext() throws Exception
   {
      fire(new org.jboss.arquillian.test.spi.event.suite.Test(new TestMethodExecutor()
      {
         @Override
         public void invoke(Object... parameters) throws Throwable { }

         @Override
         public Method getMethod()
         {
            return nonExistingOperatesOnDeploymentMethod();
         }

         @Override
         public Object getInstance()
         {
            return ContainerEventControllerTestCase.this;
         }
      }));
   }

   @Test
   public void shouldInvokeAfterInContainerDeploymentContext() throws Exception
   {
      fire(new After(this, testMethod()));
      
      assertEventFiredInContext(After.class, ContainerContext.class);
      assertEventFiredInContext(After.class, DeploymentContext.class);
   }

   @Test @Ignore
   public void shouldEnrichTestInstance() throws Exception
   {
      fire(new org.jboss.arquillian.test.spi.event.suite.Before(testClass(), testMethod()));
      
      //assertEventFired(Enrich, count)
   }

   private Class<?> testClass()
   {
      return ContainerEventControllerTestCase.class;
   }

   private Method testMethod()
   {
      try
      {
         return ContainerEventControllerTestCase.class.getDeclaredMethod("testMethod");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @OperateOnDeployment("NON_EXISTING_DEPLOYMENT")
   private Method nonExistingOperatesOnDeploymentMethod()
   {
      try
      {
         return ContainerEventControllerTestCase.class.getDeclaredMethod("nonExistingOperatesOnDeploymentMethod");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
