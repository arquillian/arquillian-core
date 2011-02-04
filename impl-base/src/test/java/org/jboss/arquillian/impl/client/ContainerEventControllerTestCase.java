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
package org.jboss.arquillian.impl.client;

import java.lang.reflect.Method;

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.client.container.event.DeployManagedDeployments;
import org.jboss.arquillian.impl.client.container.event.SetupContainers;
import org.jboss.arquillian.impl.client.container.event.StartManagedContainers;
import org.jboss.arquillian.impl.client.container.event.StopManagedContainers;
import org.jboss.arquillian.impl.client.container.event.UnDeployManagedDeployments;
import org.jboss.arquillian.impl.client.event.ActivateContainerDeploymentContext;
import org.jboss.arquillian.impl.client.event.DeActivateContainerDeploymentContext;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.event.suite.After;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.junit.Test;

/**
 * ContainerBeforeAfterControllerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerEventControllerTestCase extends AbstractManagerTestBase
{
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extensions(ContainerEventController.class);
   }
   
   @Test
   public void shouldSetupAndStartContainers() throws Exception
   {
      fire(new BeforeSuite());
      
      assertEventFired(SetupContainers.class, 1);
      assertEventFired(StartManagedContainers.class, 1);
   }
   
   @Test
   public void shouldStopContainers() throws Exception
   {
      fire(new AfterSuite());
      
      assertEventFired(StopManagedContainers.class, 1);
   }

   @Test
   public void shouldDeployManagedDeployments() throws Exception
   {
      fire(new BeforeClass(testClass()));
      
      assertEventFired(DeployManagedDeployments.class, 1);
   }
   
   @Test
   public void shouldUnDeployManagedDeployments() throws Exception
   {
      fire(new AfterClass(testClass()));
      
      assertEventFired(UnDeployManagedDeployments.class, 1);
   }

   @Test
   public void shouldActiveDeploymentContext() throws Exception
   {
      fire(new Before(this, testMethod()));
      
      assertEventFired(ActivateContainerDeploymentContext.class, 1);
   }
   
   @Test
   public void shouldDeActiveDeploymentContext() throws Exception
   {
      fire(new After(this, testMethod()));
      
      assertEventFired(DeActivateContainerDeploymentContext.class, 1);
   }

   @Test
   public void shouldEnrichTestInstance() throws Exception
   {
      fire(new Before(testClass(), testMethod()));
      
      //assertEventFired(Enrich, count)
   }

   private Class<?> testClass()
   {
      return ContainerEventControllerTestCase.class;
   }
   
   private Method testMethod() throws Exception
   {
      return ContainerEventControllerTestCase.class.getDeclaredMethod("testMethod");
   }
}
