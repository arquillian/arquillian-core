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

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Verify the that JUnit integration adaptor fires the expected events even when Handlers are failing.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class JUnitIntegrationTestCase extends JUnitTestBaseClass 
{
   @Test
   public void shouldNotCallAnyMethodsWithoutLifecycleHandlers() throws Exception 
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      when(adaptor.test(isA(TestMethodExecutor.class))).thenReturn(new TestResult(Status.PASSED));
      
      Result result = run(adaptor, ArquillianClass1.class);

      Assert.assertTrue(result.wasSuccessful());
      assertCycle(0, Cycle.values());
      
      verify(adaptor, times(1)).beforeSuite();
      verify(adaptor, times(1)).afterSuite();
   }
   
   @Test
   public void shouldCallAllMethods() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);
      
      Result result = run(adaptor, ArquillianClass1.class);
      
      Assert.assertTrue(result.wasSuccessful());
      assertCycle(1, Cycle.values());

      verify(adaptor, times(1)).beforeSuite();
      verify(adaptor, times(1)).afterSuite();
   }
   
   @Test
   public void shouldCallAfterClassWhenBeforeThrowsException() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);
      
      throwException(Cycle.BEFORE_CLASS, new Throwable());
      
      Result result = run(adaptor, ArquillianClass1.class);
      Assert.assertFalse(result.wasSuccessful());
      
      assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS);
      assertCycle(0, Cycle.BEFORE, Cycle.AFTER, Cycle.TEST);
   
      verify(adaptor, times(1)).beforeSuite();
      verify(adaptor, times(1)).afterSuite();
   }

   @Test
   public void shouldCallAfterWhenBeforeThrowsException() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);
      
      throwException(Cycle.BEFORE, new Throwable());
      
      Result result = run(adaptor, ArquillianClass1.class);
      Assert.assertFalse(result.wasSuccessful());
      
      assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS, Cycle.BEFORE, Cycle.AFTER);
      assertCycle(0, Cycle.TEST);

      verify(adaptor, times(1)).beforeSuite();
      verify(adaptor, times(1)).afterSuite();
   }
   
   @Test
   public void shouldOnlyCallBeforeAfterSuiteOnce() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);
      
      Result result = run(adaptor, ArquillianClass1.class, ArquillianClass1.class, ArquillianClass1.class, ArquillianClass1.class);
      Assert.assertTrue(result.wasSuccessful());

      verify(adaptor, times(1)).beforeSuite();
      verify(adaptor, times(1)).afterSuite();
   }
   
   /*
    * ARQ-391, After not called when Error's are thrown, e.g. AssertionError
    */
   @Test
   public void shouldCallAllWhenTestThrowsException() throws Exception
   {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      executeAllLifeCycles(adaptor);

      throwException(Cycle.TEST, new Throwable());

      Result result = run(adaptor, ArquillianClass1.class);
      Assert.assertFalse(result.wasSuccessful());

      assertCycle(1, Cycle.values());

      verify(adaptor, times(1)).beforeSuite();
      verify(adaptor, times(1)).afterSuite();
   }

   @Test
   public void shouldWorkWithTimeout() throws Exception {
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

      executeAllLifeCycles(adaptor);

      Result result = run(adaptor, ArquillianClass1WithTimeout.class);

      Assert.assertFalse(result.wasSuccessful());
      Assert.assertTrue(result.getFailures().get(0).getMessage().contains("timed out"));
      assertCycle(1, Cycle.BEFORE_CLASS, Cycle.BEFORE, Cycle.AFTER, Cycle.AFTER_CLASS);

      verify(adaptor, times(1)).beforeSuite();
      verify(adaptor, times(1)).afterSuite();
   }
}
