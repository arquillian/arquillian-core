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
import java.util.Stack;

import org.jboss.arquillian.impl.context.ContextLifecycleManager;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.spi.Context;
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
   private ContextLifecycleManager contextLifecycle;
   private Stack<Context> activeContext = new Stack<Context>();
   
   public EventTestRunnerAdaptor(ContextLifecycleManager contextLifecycle)
   {
      Validate.notNull(contextLifecycle, "ContextLifecycle must be specified");
      
      this.contextLifecycle = contextLifecycle;
   }

   public Context getActiveContext()
   {
      return activeContext.peek();
   }
   
   public void beforeSuite() throws Exception
   {
      Context suiteContext = contextLifecycle.createRestoreSuiteContext();
      suiteContext.fire(new BeforeSuite());
      activeContext.push(suiteContext);
   }

   public void afterSuite() throws Exception
   {
      contextLifecycle.createRestoreSuiteContext().fire(new AfterSuite());
      activeContext.pop();
      contextLifecycle.destroySuiteContext();
   }

   public void beforeClass(Class<?> testClass) throws Exception
   {
      Validate.notNull(testClass, "TestClass must be specified");
      
      Context classContext = contextLifecycle.createRestoreClassContext(testClass);
      classContext.fire(new BeforeClass(testClass));
      activeContext.push(classContext);
   }

   public void afterClass(Class<?> testClass) throws Exception
   {
      Validate.notNull(testClass, "TestClass must be specified");
      
      contextLifecycle.createRestoreClassContext(testClass).fire(new AfterClass(testClass));
      activeContext.pop();
      contextLifecycle.destroyClassContext(testClass);
   }

   public void before(Object testInstance, Method testMethod) throws Exception
   {
      Validate.notNull(testInstance, "TestInstance must be specified");
      Validate.notNull(testMethod, "TestMethod must be specified");
      
      TestContext testContext = contextLifecycle.createRestoreTestContext(testInstance);
      testContext.fire(new Before(testInstance, testMethod));
      activeContext.push(testContext);
   }

   public void after(Object testInstance, Method testMethod) throws Exception
   {
      Validate.notNull(testInstance, "TestInstance must be specified");
      Validate.notNull(testMethod, "TestMethod must be specified");

      contextLifecycle.createRestoreTestContext(testInstance).fire(new After(testInstance, testMethod));
      activeContext.pop();
      contextLifecycle.destroyTestContext(testInstance);
   }
   
   public TestResult test(TestMethodExecutor testMethodExecutor) throws Exception
   {
      Validate.notNull(testMethodExecutor, "TestMethodExecutor must be specified");
      
      Test test = new Test(testMethodExecutor);
      TestContext context = contextLifecycle.createRestoreTestContext(testMethodExecutor.getInstance());
      context.fire(test);
      return context.get(TestResult.class);
   }
}
