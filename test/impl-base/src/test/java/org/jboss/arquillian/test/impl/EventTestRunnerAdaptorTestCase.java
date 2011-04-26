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
package org.jboss.arquillian.test.impl;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.event.suite.After;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Verifies that the {@link EventTestRunnerAdaptor} creates and fires the proper events.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class EventTestRunnerAdaptorTestCase extends AbstractTestTestBase
{

   @Override
   protected void addExtensions(List<Class<?>> extensions) 
   {  
      extensions.add(TestContextHandler.class);
   }
   
   @Override
   protected void startContexts(Manager manager)
   {
      // this is a test of the Context activation, don't auto start
   }

   @Test
   public void shouldHandleLifeCycleEvents() throws Exception 
   {
      Manager manager = getManager();
      EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(manager);
      
      Class<?> testClass = getClass();
      Method testMethod = testClass.getMethod("shouldHandleLifeCycleEvents");
      Object testInstance = this;
      
      TestMethodExecutor testExecutor = Mockito.mock(TestMethodExecutor.class);
      Mockito.when(testExecutor.getInstance()).thenReturn(testInstance);
      Mockito.when(testExecutor.getMethod()).thenReturn(testMethod);
      
      verifyNoActiveContext(manager);

      adaptor.beforeSuite();
      assertEventFired(BeforeSuite.class, 1);
      assertEventFiredInContext(BeforeSuite.class, SuiteContext.class);

      verifyNoActiveContext(manager);
      
      adaptor.beforeClass(testClass, LifecycleMethodExecutor.NO_OP);
      assertEventFired(BeforeClass.class, 1);
      assertEventFiredInContext(BeforeClass.class, SuiteContext.class);
      assertEventFiredInContext(BeforeClass.class, ClassContext.class);

      verifyNoActiveContext(manager);

      adaptor.before(testInstance, testMethod, LifecycleMethodExecutor.NO_OP);
      assertEventFired(Before.class, 1);
      assertEventFiredInContext(Before.class, SuiteContext.class);
      assertEventFiredInContext(Before.class, ClassContext.class);
      assertEventFiredInContext(Before.class, TestContext.class);

      verifyNoActiveContext(manager);
      
      adaptor.test(testExecutor);
      assertEventFired(org.jboss.arquillian.spi.event.suite.Test.class, 1);
      assertEventFiredInContext(org.jboss.arquillian.spi.event.suite.Test.class, SuiteContext.class);
      assertEventFiredInContext(org.jboss.arquillian.spi.event.suite.Test.class, ClassContext.class);
      assertEventFiredInContext(org.jboss.arquillian.spi.event.suite.Test.class, TestContext.class);

      verifyNoActiveContext(manager);
      
      adaptor.after(testInstance, testMethod, LifecycleMethodExecutor.NO_OP);
      assertEventFired(After.class, 1);
      assertEventFiredInContext(After.class, SuiteContext.class);
      assertEventFiredInContext(After.class, ClassContext.class);
      assertEventFiredInContext(After.class, TestContext.class);
      
      verifyNoActiveContext(manager);

      adaptor.afterClass(testClass, LifecycleMethodExecutor.NO_OP);
      assertEventFired(AfterClass.class, 1);
      assertEventFiredInContext(AfterClass.class, SuiteContext.class);
      assertEventFiredInContext(AfterClass.class, ClassContext.class);

      verifyNoActiveContext(manager);

      adaptor.afterSuite();
      assertEventFired(AfterSuite.class, 1);
      assertEventFiredInContext(AfterSuite.class, SuiteContext.class);

      verifyNoActiveContext(manager);
   }

   private void verifyNoActiveContext(Manager manager)
   {
      verify(false, false, false, manager);
   }
   
   private void verify(boolean suite, boolean clazz, boolean test, Manager manager)
   {
      Assert.assertEquals(
            "SuiteContext should" + (!suite ? " not":"") + " be active",
            suite, manager.getContext(SuiteContext.class).isActive());
      Assert.assertEquals(
            "ClassContext should" + (!suite ? " not":"") + " be active",
            clazz, manager.getContext(ClassContext.class).isActive());
      Assert.assertEquals(
            "TestContext should" + (!suite ? " not":"") + " be active",
            test, manager.getContext(TestContext.class).isActive());
   }
}
