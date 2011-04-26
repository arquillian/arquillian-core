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

import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.TestRunner;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

/**
 * JUnitTestRunner
 * 
 * A Implementation of the Arquillian TestRunner SPI for JUnit.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JUnitTestRunner implements TestRunner
{
   /** 
    * Overwrite to provide additional run listeners. 
    */
   protected List<RunListener> getRunListeners()
   {
      return Collections.emptyList();
   }

   public TestResult execute(Class<?> testClass, String methodName)
   {
      JUnitCore runner = new JUnitCore();

      ExpectedExceptionHolder exceptionHolder = new ExpectedExceptionHolder();
      runner.addListener(exceptionHolder);

      for (RunListener listener : getRunListeners())
         runner.addListener(listener);

      Result result = runner.run(Request.method(testClass, methodName));

      return convertToTestResult(result, exceptionHolder.getException());
   }

   /**
    * Convert a JUnit Result object to Arquillian TestResult
    * 
    * @param result JUnit Test Run Result
    * @return The TestResult representation of the JUnit Result
    */
   private TestResult convertToTestResult(Result result, Throwable expectedException)
   {
      Status status = Status.PASSED;
      Throwable throwable = expectedException;
      if (result.getFailureCount() > 0)
      {
         status = Status.FAILED;
         throwable = result.getFailures().get(0).getException();
      }
      if (result.getIgnoreCount() > 0)
      {
         status = Status.SKIPPED;
      }
      return new TestResult(status, throwable);
   }

   private class ExpectedExceptionHolder extends RunListener
   {
      private Throwable exception;

      public Throwable getException()
      {
         return exception;
      }

      @Override
      public void testFinished(Description description) throws Exception
      {
         Test test = description.getAnnotation(Test.class);
         if (test != null && test.expected() != Test.None.class)
         {
            exception = Arquillian.caughtTestException.get();
            Arquillian.caughtTestException.set(null);
         }
      }
   }
}
