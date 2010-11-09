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
package org.jboss.arquillian.impl.handler;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * InContainerExecuterTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEventExecuterTestCase extends AbstractManagerTestBase
{
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(TestEventExecuter.class);
   }
   
   @Mock
   private TestMethodExecutor testExecutor;

   @Test
   public void shouldReturnPassed() throws Throwable
   {
      Mockito.when(testExecutor.getInstance()).thenReturn(this);
      Mockito.when(testExecutor.getMethod()).thenReturn(
            getTestMethod("shouldReturnPassed"));
      
      org.jboss.arquillian.spi.event.suite.Test event = new org.jboss.arquillian.spi.event.suite.Test(testExecutor);
   
      fire(event);
      
      TestResult result = getManager().resolve(TestResult.class);
      Assert.assertNotNull(
            "Should have set result",
            result);

      Assert.assertEquals(
            "Should have passed test",
            TestResult.Status.PASSED,
            result.getStatus());

      Assert.assertNull(
            "Should not have set cause",
            result.getThrowable());
   }

   @Test
   public void shouldReturnFailedOnException() throws Throwable
   {
      Exception exception = new Exception();
      
      Mockito.when(testExecutor.getInstance()).thenReturn(this);
      Mockito.when(testExecutor.getMethod()).thenReturn(
            getTestMethod("shouldReturnFailedOnException"));
      Mockito.doThrow(exception).when(testExecutor).invoke();
      
      org.jboss.arquillian.spi.event.suite.Test event = new org.jboss.arquillian.spi.event.suite.Test(testExecutor);

      fire(event);

      TestResult result = getManager().resolve(TestResult.class);
      Assert.assertNotNull(
            "Should have set result",
            result);

      Assert.assertEquals(
            "Should have failed test",
            TestResult.Status.FAILED,
            result.getStatus());

      Assert.assertEquals(
            "Should have set failed cause",
            exception,
            result.getThrowable());
   }

   private Method getTestMethod(String name) throws Exception
   {
      return this.getClass().getMethod(name);
   }
}
