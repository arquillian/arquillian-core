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

import org.jboss.arquillian.impl.DeployableTest;
import org.jboss.arquillian.impl.TestResultImpl;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunner;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.shrinkwrap.impl.base.Validate;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

/**
 * JUnitTestRunner
 * 
 * A Implementation of the Arquillian TestRunner SPI for JUnit.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JUnitTestRunner implements TestRunner
{
   private ExecutionMode executionMode = ExecutionMode.STANDALONE; 
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunner#setExecutionMode(org.jboss.arquillian.spi.TestRunner.ExecutionMode)
    */
   @Override
   public void setExecutionMode(ExecutionMode executionMode)
   {
      Validate.notNull(executionMode, "ExecutionMode must be specified");
      this.executionMode = executionMode;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunner#execute(java.lang.Class, java.lang.String)
    */
   @Override
   public TestResult execute(Class<?> testClass, String methodName)
   {
      setExecutionMode();
      
      JUnitCore runner = new JUnitCore();
      Result result = runner.run(
            Request.method(
                  testClass, 
                  methodName));
      
      TestResult testResult = convertToTestResult(result);
      
      resetExecutionMode();
      
      return testResult;
   }

   private void setExecutionMode() 
   {
      switch (executionMode)
      {
         case CONTAINER:
            DeployableTest.setInContainer(true);
            break;
      }
   }
   
   private void resetExecutionMode() 
   {
      switch (executionMode)
      {
         case CONTAINER:
            DeployableTest.setInContainer(false);
            break;
      }
   }

   /**
    * Convert a JUnit Result object to Arquillian TestResult
    * 
    * @param result JUnit Test Run Result
    * @return The TestResult representation of the JUnit Result
    */
   private TestResult convertToTestResult(Result result) 
   {
      Status status = Status.PASSED;
      Throwable throwable = null;
      
      if(result.getFailureCount() > 0) 
      {
         status = Status.FAILED;
         throwable = result.getFailures().get(0).getException();
      }
      if(result.getIgnoreCount() > 0) 
      {
         status = Status.SKIPPED;
      }
      return new TestResultImpl(status, throwable);
   }
}
