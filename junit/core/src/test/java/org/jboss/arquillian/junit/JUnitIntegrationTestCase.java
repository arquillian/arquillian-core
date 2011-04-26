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
package org.jboss.arquillian.junit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.arquillian.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.TestRunnerAdaptor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;


/**
 * Verify the that JUnit integration adaptor fires the expected events even when Handlers are failing.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class JUnitIntegrationTestCase 
{
   public static enum Cycle { BEFORE_CLASS, BEFORE, TEST,  AFTER, AFTER_CLASS }
   
   @Test
   public void shouldNotCallAnyMethodsWithoutLifecycleHandlers() throws Exception 
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      when(adaptor.test(isA(TestMethodExecutor.class))).thenReturn(new TestResult(Status.PASSED));
      
      Result result = run(adaptor, ArquillianClass1.class);

      Assert.assertTrue(result.wasSuccessful());
      assertCycle(0, Cycle.values());
   }
   
   @Test
   public void shouldCallAllMethods() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);
      
      Result result = run(adaptor, ArquillianClass1.class);
      
      Assert.assertTrue(result.wasSuccessful());
      assertCycle(1, Cycle.values());
   }
   
   @Test
   public void shouldCallAfterClassWhenBeforeThrowsException() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);
      
      callbackException.put(Cycle.BEFORE_CLASS, new Throwable());
      
      Result result = run(adaptor, ArquillianClass1.class);
      Assert.assertFalse(result.wasSuccessful());
      
      assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS);
      assertCycle(0, Cycle.BEFORE, Cycle.AFTER, Cycle.TEST);
   }

   @Test
   public void shouldCallAfterWhenBeforeThrowsException() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);
      
      callbackException.put(Cycle.BEFORE, new Throwable());
      
      Result result = run(adaptor, ArquillianClass1.class);
      Assert.assertFalse(result.wasSuccessful());
      
      assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS, Cycle.BEFORE, Cycle.AFTER);
      assertCycle(0, Cycle.TEST);
   }

   /*
    * ARQ-391, After not called when Error's are thrown, e.g. AssertionError
    */
   @Test
   public void shouldCallAllWhenTestThrowsException() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);
      
      callbackException.put(Cycle.TEST, new Throwable());
      
      Result result = run(adaptor, ArquillianClass1.class);
      Assert.assertFalse(result.wasSuccessful());
      
      assertCycle(1, Cycle.values());
   }

   /*
    * Internal Helpers
    */
   private void executeAllLifeCycles(TestRunnerAdaptor adaptor) throws Exception
   {
      doAnswer(new ExecuteLifecycle()).when(adaptor).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
      doAnswer(new ExecuteLifecycle()).when(adaptor).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
      doAnswer(new ExecuteLifecycle()).when(adaptor).before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
      doAnswer(new ExecuteLifecycle()).when(adaptor).after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
      doAnswer(new TestExecuteLifecycle(new TestResult(Status.PASSED))).when(adaptor).test(any(TestMethodExecutor.class));
   }
   
   public void assertCycle(int count, Cycle... cycles)
   {
      for(Cycle cycle : cycles)
      {
         Assert.assertEquals("Verify " + cycle +  " called N times", 
               count, (int)callbackCount.get(cycle));
      }
   }
   
   private Result run(TestRunnerAdaptor adaptor, Class<?>... classes)
      throws Exception
   {
      Result result = new Result();
      RunNotifier notifier = new RunNotifier();
      notifier.addFirstListener(result.createListener());
      for(Class<?> clazz : classes)
      {
         new Arquillian(clazz, adaptor).run(notifier);   
      }
      
      notifier.fireTestRunFinished(result);

      return result;
   }
   
   /*
    * Setup / Clear the static callback info. 
    */
   public static Map<Cycle, Integer> callbackCount = new HashMap<Cycle, Integer>();
   public static Map<Cycle, Throwable> callbackException = new HashMap<Cycle, Throwable>();
   static 
   {
      for(Cycle tmp : Cycle.values())
      {
         callbackCount.put(tmp, 0);   
      }
   }
   
   public static void wasCalled(Cycle cycle) throws Throwable
   {
      if(callbackCount.containsKey(cycle))
      {
         callbackCount.put(cycle, callbackCount.get(cycle) + 1);
      }
      else 
      {
         throw new RuntimeException("Unknown callback: " + cycle);
      }
      if(callbackException.containsKey(cycle))
      {
         throw callbackException.get(cycle);
      }
   }

   @After
   public void clearCallbacks()
   {
      callbackCount.clear();
      for(Cycle tmp : Cycle.values())
      {
         callbackCount.put(tmp, 0);   
      }
      callbackException.clear();
   }

   /*
    * Mockito Answers for invoking the LifeCycle callbacks.
    */
   private static class ExecuteLifecycle implements Answer<Object>
   {
      @Override
      public Object answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable 
      {
         for(Object argument : invocation.getArguments())
         {
            if(argument instanceof LifecycleMethodExecutor)
            {
               ((LifecycleMethodExecutor)argument).invoke();
            }
            else if(argument instanceof TestMethodExecutor)
            {
               ((TestMethodExecutor)argument).invoke();               
            }
         }
         return null;
      }
   }
   
   private static class TestExecuteLifecycle extends ExecuteLifecycle
   {
      private TestResult result;

      public TestExecuteLifecycle(TestResult result)
      {
         this.result = result;
      }
      
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable
      {
         super.answer(invocation);
         return result;
      }
   }

   /*
    * Predfined TestClass 
    */
   @RunWith(Arquillian.class)
   public static class ArquillianClass1 
   {
      @BeforeClass
      public static void beforeClass() throws Throwable
      {
         wasCalled(Cycle.BEFORE_CLASS);
      }

      @AfterClass
      public static void afterClass() throws Throwable
      {
         wasCalled(Cycle.AFTER_CLASS);
      }

      @Before
      public void before() throws Throwable
      {
         wasCalled(Cycle.BEFORE);
      }

      @After
      public void after() throws Throwable
      {
         wasCalled(Cycle.AFTER);
      }

      @Test
      public void shouldBeInvoked() throws Throwable 
      {
         wasCalled(Cycle.TEST);
      }
   }
}
