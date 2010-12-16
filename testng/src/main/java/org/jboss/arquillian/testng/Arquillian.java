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
package org.jboss.arquillian.testng;

import java.lang.reflect.Method;

import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunnerAdaptor;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

/**
 * Arquillian
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class Arquillian implements IHookable
{
   public static final String ARQUILLIAN_DATA_PROVIDER = "ARQUILLIAN_DATA_PROVIDER";
   
   private static ThreadLocal<TestRunnerAdaptor> deployableTest = new ThreadLocal<TestRunnerAdaptor>();

   @BeforeSuite(alwaysRun = true)
   public void arquillianBeforeSuite() throws Exception
   {
      if(deployableTest.get() == null)
      {
         TestRunnerAdaptor adaptor = DeployableTestBuilder.build();
         adaptor.beforeSuite(); 
         deployableTest.set(adaptor); // don't set TestRunnerAdaptor if beforeSuite fails
      }
   }

   @AfterSuite(alwaysRun = true)
   public void arquillianAfterSuite() throws Exception
   {
      if (deployableTest.get() == null) 
      {
         return; // beforeSuite failed
      }
      deployableTest.get().afterSuite();
      deployableTest.get().shutdown();
      deployableTest.set(null);
      deployableTest.remove();
   }

   @BeforeClass(alwaysRun = true)
   public void arquillianBeforeClass() throws Exception
   {
      deployableTest.get().beforeClass(getClass());
   }

   @AfterClass(alwaysRun = true)
   public void arquillianAfterClass() throws Exception
   {
      deployableTest.get().afterClass(getClass());
   }
   
   @BeforeMethod(alwaysRun = true)
   public void arquillianBeforeTest(Method testMethod) throws Exception 
   {
      deployableTest.get().before(this, testMethod);
   }

   @AfterMethod(alwaysRun = true)
   public void arquillianAfterTest(Method testMethod) throws Exception 
   {
      deployableTest.get().after(this, testMethod);
   }

   public void run(final IHookCallBack callback, final ITestResult testResult)
   {
      TestResult result;
      try
      {
         result = deployableTest.get().test(new TestMethodExecutor()
         {
            public void invoke() throws Throwable
            {
               callback.runTestMethod(testResult);
               
               clearParameters(testResult);
            }

            private void clearParameters(final ITestResult testResult)
            {
               // clear parameters. they can be contextual and might fail TestNG during the report writing.
               Object[] parameters = testResult.getParameters();
               for(int i = 0; parameters != null && i < parameters.length; i++)
               {
                  Object parameter = parameters[i];
                  if(parameter != null)
                  {
                     parameters[i] = parameter.getClass().getName();
                  }
                  else
                  {
                     parameters[i] = "null";
                  }
               }
            }
            
            public Method getMethod()
            {
               return testResult.getMethod().getMethod();
            }
            
            public Object getInstance()
            {
               return Arquillian.this;
            }
         });
         if(result.getThrowable() != null)
         {
            testResult.setThrowable(result.getThrowable());
         }

         // calculate test end time. this is overwritten in the testng invoker.. 
         testResult.setEndMillis( (result.getStart() - result.getEnd()) + testResult.getStartMillis());
      } 
      catch (Exception e)
      {
         testResult.setThrowable(e);
      }
   }
   
   @DataProvider(name = Arquillian.ARQUILLIAN_DATA_PROVIDER)
   public Object[][] arquillianArgumentProvider(Method method) 
   {
      Object[][] values = new Object[1][method.getParameterTypes().length];
      
      if (deployableTest.get() == null)
      {
         return values;
      }

      // TestEnrichers.enrich(deployableTest.get().getActiveContext(), method); // should be handled inside a enricher
      Object[] parameterValues = new Object[method.getParameterTypes().length]; 
      values[0] = parameterValues; 
      
      return values;
   }
}
