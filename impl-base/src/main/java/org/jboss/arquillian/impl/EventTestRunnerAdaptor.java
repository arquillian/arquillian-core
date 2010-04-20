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

import org.jboss.arquillian.impl.context.ContextLifecycleManager;
import org.jboss.arquillian.impl.event.type.After;
import org.jboss.arquillian.impl.event.type.AfterClass;
import org.jboss.arquillian.impl.event.type.AfterSuite;
import org.jboss.arquillian.impl.event.type.Before;
import org.jboss.arquillian.impl.event.type.BeforeClass;
import org.jboss.arquillian.impl.event.type.BeforeSuite;
import org.jboss.arquillian.impl.event.type.Test;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunnerAdaptor;

/**
 * EventTestRunnerAdaptor
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EventTestRunnerAdaptor implements TestRunnerAdaptor
{
   private ContextLifecycleManager contextLifecycle;
   
   public EventTestRunnerAdaptor(ContextLifecycleManager contextLifecycle)
   {
      Validate.notNull(contextLifecycle, "ContextLifecycle must be specified");
      
      this.contextLifecycle = contextLifecycle;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunnerAdaptor#beforeSuite()
    */
   @Override
   public void beforeSuite() throws Exception
   {
      contextLifecycle.createRestoreSuiteContext().fire(new BeforeSuite());
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunnerAdaptor#afterSuite()
    */
   @Override
   public void afterSuite() throws Exception
   {
      contextLifecycle.createRestoreSuiteContext().fire(new AfterSuite());
      contextLifecycle.destroySuiteContext();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunnerAdaptor#beforeClass(java.lang.Class)
    */
   @Override
   public void beforeClass(Class<?> testClass) throws Exception
   {
      Validate.notNull(testClass, "TestClass must be specified");
      
      contextLifecycle.createRestoreClassContext(testClass).fire(new BeforeClass(testClass));
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunnerAdaptor#afterClasss(java.lang.Class)
    */
   @Override
   public void afterClass(Class<?> testClass) throws Exception
   {
      Validate.notNull(testClass, "TestClass must be specified");
      
      contextLifecycle.createRestoreClassContext(testClass).fire(new AfterClass(testClass));
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunnerAdaptor#before(java.lang.Object, java.lang.reflect.Method)
    */
   @Override
   public void before(Object testInstance, Method testMethod) throws Exception
   {
      Validate.notNull(testInstance, "TestInstance must be specified");
      Validate.notNull(testMethod, "TestMethod must be specified");
      
      contextLifecycle.createRestoreTestContext(testInstance).fire(new Before(testInstance, testMethod));
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunnerAdaptor#after(java.lang.Object, java.lang.reflect.Method)
    */
   @Override
   public void after(Object testInstance, Method testMethod) throws Exception
   {
      Validate.notNull(testInstance, "TestInstance must be specified");
      Validate.notNull(testMethod, "TestMethod must be specified");

      contextLifecycle.createRestoreTestContext(testInstance).fire(new After(testInstance, testMethod));
      contextLifecycle.destroyTestContext(testInstance);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunnerAdaptor#test(org.jboss.arquillian.spi.TestMethodExecutor)
    */
   @Override
   public TestResult test(TestMethodExecutor testMethodExecutor) throws Exception
   {
      Validate.notNull(testMethodExecutor, "TestMethodExecutor must be specified");
      
      Test test = new Test(testMethodExecutor);
      contextLifecycle.createRestoreTestContext(testMethodExecutor.getInstance()).fire(test);
      return test.getTestResult();
   }
}
