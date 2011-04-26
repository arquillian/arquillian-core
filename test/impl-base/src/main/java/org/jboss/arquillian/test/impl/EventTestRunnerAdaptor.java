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
package org.jboss.arquillian.test.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.core.spi.NonManagedObserver;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunnerAdaptor;
import org.jboss.arquillian.spi.event.suite.After;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.spi.event.suite.Test;

/**
 * EventTestRunnerAdaptor
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EventTestRunnerAdaptor implements TestRunnerAdaptor
{
   private Manager manager;
   
   public EventTestRunnerAdaptor(ManagerBuilder builder)
   {
      Validate.notNull(builder, "ManagerBuilder must be specified");
      
      this.manager = builder.create();
      this.manager.start();
   }

   public EventTestRunnerAdaptor(Manager manager)
   {
      Validate.notNull(manager, "Manager must be specified");
      
      this.manager = manager;
   }

   public void beforeSuite() throws Exception
   {
      manager.fire(new BeforeSuite());
   }

   public void afterSuite() throws Exception
   {
      manager.fire(new AfterSuite());
   }

   public void beforeClass(Class<?> testClass, LifecycleMethodExecutor executor) throws Exception
   {
      Validate.notNull(testClass, "TestClass must be specified");
      
      manager.fire(new BeforeClass(testClass, executor));
   }

   public void afterClass(Class<?> testClass, LifecycleMethodExecutor executor) throws Exception
   {
      Validate.notNull(testClass, "TestClass must be specified");
      
      manager.fire(new AfterClass(testClass, executor));
   }

   public void before(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception
   {
      Validate.notNull(testInstance, "TestInstance must be specified");
      Validate.notNull(testMethod, "TestMethod must be specified");
      
      manager.fire(new Before(testInstance, testMethod, executor));
   }

   public void after(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception
   {
      Validate.notNull(testInstance, "TestInstance must be specified");
      Validate.notNull(testMethod, "TestMethod must be specified");

      manager.fire(new After(testInstance, testMethod, executor));
   }
   
   public TestResult test(TestMethodExecutor testMethodExecutor) throws Exception
   {
      Validate.notNull(testMethodExecutor, "TestMethodExecutor must be specified");

      final List<TestResult> result = new ArrayList<TestResult>();
      manager.fire(new Test(testMethodExecutor), new NonManagedObserver<Test>()
      {
         @Inject
         private Instance<TestResult> testResult;
         
         @Override
         public void fired(Test event)
         {
            result.add(testResult.get());
         }
      });
      return result.get(0);
   }
   
   public void shutdown()
   {
      manager.shutdown();
   }
}
