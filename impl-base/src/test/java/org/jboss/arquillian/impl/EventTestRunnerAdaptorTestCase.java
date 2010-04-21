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

import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.ContextLifecycleManager;
import org.jboss.arquillian.impl.context.ProfileBuilder;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.impl.event.EventHandler;
import org.jboss.arquillian.impl.event.type.ClassEvent;
import org.jboss.arquillian.impl.event.type.SuiteEvent;
import org.jboss.arquillian.impl.event.type.TestEvent;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestMethodExecutor;
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
   private EventHandler<SuiteContext, SuiteEvent> suiteEventHandler;

   @Mock
   private EventHandler<ClassContext, ClassEvent> classEventHandler;

   @Mock
   private EventHandler<TestContext, TestEvent> testEventHandler;

   @Before
   public void createContexts() throws Exception 
   {
      // Add mock EventHandlers to the contexts so we can verify that the events are fired
      Mockito.doAnswer(new Answer<Void>()
      {
         
         public Void answer(InvocationOnMock invocation) throws Throwable
         {
            SuiteContext context = (SuiteContext)invocation.getArguments()[0];
            context.register(org.jboss.arquillian.impl.event.type.BeforeSuite.class, suiteEventHandler);
            context.register(org.jboss.arquillian.impl.event.type.AfterSuite.class, suiteEventHandler);
            return null;
         }
      }).when(profileBuilder).buildSuiteContext(Mockito.any(SuiteContext.class));
      Mockito.doAnswer(new Answer<Void>()
            {
               
               public Void answer(InvocationOnMock invocation) throws Throwable
               {
                  ClassContext context = (ClassContext)invocation.getArguments()[0];
                  context.register(org.jboss.arquillian.impl.event.type.BeforeClass.class, classEventHandler);
                  context.register(org.jboss.arquillian.impl.event.type.AfterClass.class, classEventHandler);
                  return null;
               }
            }).when(profileBuilder).buildClassContext(Mockito.any(ClassContext.class));
      Mockito.doAnswer(new Answer<Void>()
            {
               
               public Void answer(InvocationOnMock invocation) throws Throwable
               {
                  TestContext context = (TestContext)invocation.getArguments()[0];
                  context.register(org.jboss.arquillian.impl.event.type.Before.class, testEventHandler);
                  context.register(org.jboss.arquillian.impl.event.type.Test.class, testEventHandler);
                  context.register(org.jboss.arquillian.impl.event.type.After.class, testEventHandler);
                  return null;
               }
            }).when(profileBuilder).buildTestContext(Mockito.any(TestContext.class));
   }
   
   @Test
   public void shouldHandleLifeCycleEvents() throws Exception 
   {
      EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(
            new ContextLifecycleManager(profileBuilder, serviceLoader));
      
      Class<?> testClass = getClass();
      Method testMetod = testClass.getMethod("shouldHandleLifeCycleEvents");
      Object testInstance = this;
      
      TestMethodExecutor testExecutor = Mockito.mock(TestMethodExecutor.class);
      Mockito.when(testExecutor.getInstance()).thenReturn(testInstance);
      Mockito.when(testExecutor.getMethod()).thenReturn(testMetod);
      
      adaptor.beforeSuite();
      adaptor.beforeClass(testClass);
      adaptor.before(testInstance, testMetod);
      adaptor.test(testExecutor);
      adaptor.after(testInstance, testMetod);
      adaptor.afterClass(testClass);
      adaptor.afterSuite();
      
      Mockito.verify(suiteEventHandler, Mockito.times(2))
             .callback(Mockito.any(SuiteContext.class), Mockito.any(SuiteEvent.class));

      Mockito.verify(classEventHandler, Mockito.times(2))
         .callback(Mockito.any(ClassContext.class), Mockito.any(ClassEvent.class));

      Mockito.verify(testEventHandler, Mockito.times(3))
         .callback(Mockito.any(TestContext.class), Mockito.any(TestEvent.class));
   }  
}
