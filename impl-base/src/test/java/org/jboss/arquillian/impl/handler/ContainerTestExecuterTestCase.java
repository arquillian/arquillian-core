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

import org.jboss.arquillian.impl.TestResultImpl;
import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.impl.handler.ContainerTestExecuter;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * TestExecutorHandlerTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerTestExecuterTestCase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock
   private ContainerMethodExecutor executor;

   @Mock
   private TestMethodExecutor testExecutor;
   
   @Test(expected = IllegalStateException.class)
   public void shouldThrowIllegalStateOnMissingContainerMethodExecutor() throws Exception
   {
      TestContext context = new TestContext(new ClassContext(new SuiteContext(serviceLoader)));
      
      Mockito.when(testExecutor.getInstance()).thenReturn(this);
      Mockito.when(testExecutor.getMethod()).thenReturn(
            getTestMethod("shouldThrowIllegalStateOnMissingContainerMethodExecutor"));
      
      ContainerTestExecuter handler = new ContainerTestExecuter();
      handler.callback(context, 
            new org.jboss.arquillian.spi.event.suite.Test(testExecutor));
   }
   
   @Test
   public void shouldInvokeContainerMethodExecutor() throws Exception 
   {
      TestResult result = new TestResultImpl(Status.PASSED);
      Mockito.when(executor.invoke(testExecutor)).thenReturn(result);
      Mockito.when(testExecutor.getInstance()).thenReturn(this);
      Mockito.when(testExecutor.getMethod()).thenReturn(
            getTestMethod("shouldInvokeContainerMethodExecutor"));

      TestContext context = new TestContext(new ClassContext(new SuiteContext(serviceLoader)));
      context.add(ContainerMethodExecutor.class, executor);
      
      ContainerTestExecuter handler = new ContainerTestExecuter();
      handler.callback(context, 
            new org.jboss.arquillian.spi.event.suite.Test(testExecutor));
   }
   
   @Test
   public void shouldSetTestResult() throws Exception 
   {
      TestResult result = new TestResultImpl(Status.PASSED);
      Mockito.when(executor.invoke(testExecutor)).thenReturn(result);
      Mockito.when(testExecutor.getInstance()).thenReturn(this);
      Mockito.when(testExecutor.getMethod()).thenReturn(
            getTestMethod("shouldSetTestResult"));

      TestContext context = new TestContext(new ClassContext(new SuiteContext(serviceLoader)));
      context.add(ContainerMethodExecutor.class, executor);
      
      org.jboss.arquillian.spi.event.suite.Test event = new org.jboss.arquillian.spi.event.suite.Test(testExecutor);
      ContainerTestExecuter handler = new ContainerTestExecuter();
      handler.callback(context, event);
      
      Assert.assertEquals(
            "Should set result on event",
            result,
            event.getTestResult());
   }
   
   private Method getTestMethod(String name) throws Exception
   {
      return this.getClass().getMethod(name);
   }
}
