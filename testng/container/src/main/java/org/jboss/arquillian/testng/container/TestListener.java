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
package org.jboss.arquillian.testng.container;

import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestListener
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestListener implements ITestListener
{

   private ITestContext context;
   
   public void onFinish(ITestContext paramITestContext)
   {
      context = paramITestContext;
   }

   public void onStart(ITestContext paramITestContext)
   {
   }

   public void onTestFailedButWithinSuccessPercentage(ITestResult paramITestResult)
   {
   }

   public void onTestFailure(ITestResult paramITestResult)
   {
   }

   public void onTestSkipped(ITestResult paramITestResult)
   {
   }

   public void onTestStart(ITestResult paramITestResult)
   {
   }

   public void onTestSuccess(ITestResult paramITestResult)
   {
   }

   public TestResult getTestResult() 
   {
      if(context.getFailedTests().size() > 0) 
      {
         return new TestResult(
               Status.FAILED, 
               context.getFailedTests().getAllResults().iterator().next().getThrowable());
      } 
      else if(context.getSkippedTests().size() > 0)
      {
         return new TestResult(Status.SKIPPED);
      }
      if(context.getPassedTests().size() > 0) 
      {
         return new TestResult(
               Status.PASSED, 
               context.getPassedTests().getAllResults().iterator().next().getThrowable());
      } 
      return new TestResult(
            Status.FAILED, 
            new RuntimeException("Unknown test result: " + context).fillInStackTrace());
   }
}
