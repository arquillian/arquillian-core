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
package org.jboss.arquillian.junit.container;


import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.container.JUnitTestRunner;
import org.jboss.arquillian.spi.TestResult;
import org.junit.Assert;
import org.junit.Test;

public class JUnitTestRunnerTestCase
{
   @Test
   public void shouldReturnPassedTest() throws Exception 
   {
      JUnitTestRunner runner = new JUnitTestRunner();
      TestResult result = runner.execute(JUnitTestRunnerTestCase.class, "shouldProvidePassingTestToRunner");
      
      Assert.assertNotNull(result);
      Assert.assertEquals(TestResult.Status.PASSED, result.getStatus());
      Assert.assertNull(result.getThrowable());
   }

   @Test
   public void shouldReturnExceptionOnPassedTest() throws Exception 
   {
      // Simulate setting the Exception like Arquillian would. This is a JUnit hack to avoid JUnit Swallowing the Exception.
      Arquillian.caughtTestException.set(new IllegalArgumentException());
      JUnitTestRunner runner = new JUnitTestRunner();
      TestResult result = runner.execute(JUnitTestRunnerTestCase.class, "shouldProvideExpectedExceptionToRunner");
      
      Assert.assertNotNull(result);
      Assert.assertEquals(TestResult.Status.PASSED, result.getStatus());
      Assert.assertNotNull(result.getThrowable());
      Assert.assertEquals(IllegalArgumentException.class, result.getThrowable().getClass());
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldProvideExpectedExceptionToRunner() throws Exception
   {
      throw new IllegalArgumentException();
   }
   
   @Test
   public void shouldProvidePassingTestToRunner() throws Exception 
   {
      Assert.assertTrue(true);
   }
}
