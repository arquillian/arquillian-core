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
package org.jboss.arquillian.impl;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.ContextLifecycleManager;
import org.jboss.arquillian.impl.context.ProfileBuilder;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.impl.event.FiredEventException;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;
import org.jboss.arquillian.spi.event.suite.TestEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;


/**
 * Verifies that the {@link EventTestRunnerAdaptor} creates and fires the proper events.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class EventTestRunnerAdaptorTestCase
{
   @Mock
   public ServiceLoader serviceLoader;
   
   @Mock
   public ProfileBuilder profileBuilder;
   
   @Mock
   private EventHandler<SuiteEvent> suiteEventHandler;

   @Mock
   private EventHandler<ClassEvent> classEventHandler;

   @Mock
   private EventHandler<TestEvent> testEventHandler;

   @Before
   public void createContexts() throws Exception 
   {
      // Add mock EventHandlers to the contexts so we can verify that the events are fired
      Mockito.doAnswer(new Answer<Void>()
      {
         
         public Void answer(InvocationOnMock invocation) throws Throwable
         {
            SuiteContext context = (SuiteContext)invocation.getArguments()[0];
            context.register(org.jboss.arquillian.spi.event.suite.BeforeSuite.class, suiteEventHandler);
            context.register(org.jboss.arquillian.spi.event.suite.AfterSuite.class, suiteEventHandler);
            return null;
         }
      }).when(profileBuilder).buildSuiteContext(Mockito.any(SuiteContext.class));
      Mockito.doAnswer(new Answer<Void>()
            {
               
               public Void answer(InvocationOnMock invocation) throws Throwable
               {
                  ClassContext context = (ClassContext)invocation.getArguments()[0];
                  context.register(org.jboss.arquillian.spi.event.suite.BeforeClass.class, classEventHandler);
                  context.register(org.jboss.arquillian.spi.event.suite.AfterClass.class, classEventHandler);
                  return null;
               }
            }).when(profileBuilder).buildClassContext(Mockito.any(ClassContext.class), Mockito.any(Class.class));
      Mockito.doAnswer(new Answer<Void>()
            {
               
               public Void answer(InvocationOnMock invocation) throws Throwable
               {
                  TestContext context = (TestContext)invocation.getArguments()[0];
                  context.register(org.jboss.arquillian.spi.event.suite.Before.class, testEventHandler);
                  context.register(org.jboss.arquillian.spi.event.suite.Test.class, testEventHandler);
                  context.register(org.jboss.arquillian.spi.event.suite.After.class, testEventHandler);
                  return null;
               }
            }).when(profileBuilder).buildTestContext(Mockito.any(TestContext.class), Mockito.any(Class.class));
   }
   
   @Test
   public void shouldHandleLifeCycleEvents() throws Exception 
   {
      EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(
            new ContextLifecycleManager(profileBuilder, serviceLoader));
      
      Class<?> testClass = getClass();
      Method testMethod = testClass.getMethod("shouldHandleLifeCycleEvents");
      Object testInstance = this;
      
      TestMethodExecutor testExecutor = Mockito.mock(TestMethodExecutor.class);
      Mockito.when(testExecutor.getInstance()).thenReturn(testInstance);
      Mockito.when(testExecutor.getMethod()).thenReturn(testMethod);
      
      adaptor.beforeSuite();
      adaptor.beforeClass(testClass);
      adaptor.before(testInstance, testMethod);
      adaptor.test(testExecutor);
      adaptor.after(testInstance, testMethod);
      adaptor.afterClass(testClass);
      adaptor.afterSuite();
      
      Mockito.verify(suiteEventHandler, Mockito.times(2))
             .callback(Mockito.any(SuiteContext.class), Mockito.any(SuiteEvent.class));

      Mockito.verify(classEventHandler, Mockito.times(2))
         .callback(Mockito.any(ClassContext.class), Mockito.any(ClassEvent.class));

      Mockito.verify(testEventHandler, Mockito.times(3))
         .callback(Mockito.any(TestContext.class), Mockito.any(TestEvent.class));
   }
   
   /*
    *  Verify that all the Context are pushed properly even when Exceptions are thrown 
    */
   @Test
   public void shouldHandleAfterCallIfBeforeSuiteFails() throws Exception 
   {
      EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(
            new ContextLifecycleManager(profileBuilder, serviceLoader));
      
      Class<?> testClass = getClass();
      Method testMethod = testClass.getMethod("shouldHandleLifeCycleEvents");
      Object testInstance = this;
      
      TestMethodExecutor testExecutor = Mockito.mock(TestMethodExecutor.class);
      Mockito.when(testExecutor.getInstance()).thenReturn(testInstance);
      Mockito.when(testExecutor.getMethod()).thenReturn(testMethod);
      
      // force BeforeClass event handler to throw Exception
      Mockito.doThrow(
               new DeploymentException("TEST"))
           .when(suiteEventHandler).callback(
                 Mockito.any(Context.class), 
                 Mockito.isA(org.jboss.arquillian.spi.event.suite.BeforeSuite.class));
      
      Assert.assertNull(
            "verify no active context before", 
            adaptor.getActiveContext());
      try
      {
         // BeforeSuite throws Exception, simulate e.g. DeploymentException
         adaptor.beforeSuite();
         Assert.fail("BeforeSuite should have thrown exeption");
      }
      catch (FiredEventException e) 
      {
      }
      Assert.assertEquals(
            "verify SuiteContext has been pushed to stack, even with exception",
            SuiteContext.class, adaptor.getActiveContext().getClass());      

      adaptor.afterSuite();
      Assert.assertNull(
            "Verify SuiteContext has been popped from stack, we're not outside any Context",
            adaptor.getActiveContext());
   }  

   @Test
   public void shouldHandleAfterCallIfBeforeClassFails() throws Exception 
   {
      EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(
            new ContextLifecycleManager(profileBuilder, serviceLoader));
      
      Class<?> testClass = getClass();
      Method testMethod = testClass.getMethod("shouldHandleLifeCycleEvents");
      Object testInstance = this;
      
      TestMethodExecutor testExecutor = Mockito.mock(TestMethodExecutor.class);
      Mockito.when(testExecutor.getInstance()).thenReturn(testInstance);
      Mockito.when(testExecutor.getMethod()).thenReturn(testMethod);
      
      // force BeforeClass event handler to throw Exception
      Mockito.doThrow(
               new DeploymentException("TEST"))
           .when(classEventHandler).callback(
                 Mockito.any(Context.class), 
                 Mockito.isA(org.jboss.arquillian.spi.event.suite.BeforeClass.class));
      
      Assert.assertNull(
            "verify no active context before", 
            adaptor.getActiveContext());
      adaptor.beforeSuite();

      Assert.assertEquals(
            "verify SuiteContext has been pushed to stack",
            SuiteContext.class, adaptor.getActiveContext().getClass());
      
      try
      {
         // BeforeClass throws Exception, simulate e.g. DeploymentException
         adaptor.beforeClass(testClass);
         Assert.fail("BeforeClass should have thrown exeption");
      }
      catch (FiredEventException e) 
      {
      }
      Assert.assertEquals(
            "verify ClassContext has been pushed to stack, even with exception",
            ClassContext.class, adaptor.getActiveContext().getClass());      

      adaptor.afterClass(testClass);
      Assert.assertEquals(
            "verify ClassContext has been popped from stack, we're now at SuiteContext",
            SuiteContext.class, adaptor.getActiveContext().getClass());
      
      adaptor.afterSuite();
      Assert.assertNull(
            "Verify SuiteContext has been popped from stack, we're not outside any Context",
            adaptor.getActiveContext());
   }  

   /*
    *  Verify that all the Context are pushed properly even when Exceptions are thrown 
    */
   @Test
   public void shouldHandleAfterCallIfBeforeFails() throws Exception 
   {
      EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(
            new ContextLifecycleManager(profileBuilder, serviceLoader));
      
      Class<?> testClass = getClass();
      Method testMethod = testClass.getMethod("shouldHandleLifeCycleEvents");
      Object testInstance = this;
      
      TestMethodExecutor testExecutor = Mockito.mock(TestMethodExecutor.class);
      Mockito.when(testExecutor.getInstance()).thenReturn(testInstance);
      Mockito.when(testExecutor.getMethod()).thenReturn(testMethod);
      
      // force Before event handler to throw Exception
      Mockito.doThrow(
               new DeploymentException("TEST"))
           .when(testEventHandler).callback(
                 Mockito.any(Context.class), 
                 Mockito.isA(org.jboss.arquillian.spi.event.suite.Before.class));
      
      Assert.assertNull(
            "verify no active context before", 
            adaptor.getActiveContext());
      adaptor.beforeSuite();

      Assert.assertEquals(
            "verify SuiteContext has been pushed to stack",
            SuiteContext.class, adaptor.getActiveContext().getClass());
      
      adaptor.beforeClass(testClass);
      Assert.assertEquals(
            "verify ClassContext has been pushed to stack",
            ClassContext.class, adaptor.getActiveContext().getClass());
      
      try
      {
         // BeforeClass throws Exception, simulate e.g. DeploymentException
         adaptor.before(testInstance, testMethod);
         Assert.fail("Before should have thrown exeption");
      }
      catch (FiredEventException e) 
      {
      }

      Assert.assertEquals(
            "verify ClassContext has been pushed to stack, even with exception",
            TestContext.class, adaptor.getActiveContext().getClass());      

      adaptor.after(testInstance, testMethod);
      Assert.assertEquals(
            "verify TestContext has been popped from stack, we're now at ClassContext",
            ClassContext.class, adaptor.getActiveContext().getClass());
      
      
      adaptor.afterClass(testClass);
      Assert.assertEquals(
            "verify ClassContext has been popped from stack, we're now at SuiteContext",
            SuiteContext.class, adaptor.getActiveContext().getClass());
      
      adaptor.afterSuite();
      Assert.assertNull(
            "Verify SuiteContext has been popped from stack, we're not outside any Context",
            adaptor.getActiveContext());
   }  
}
