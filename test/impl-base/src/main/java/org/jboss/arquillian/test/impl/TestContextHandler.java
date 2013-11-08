/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.ClassEvent;
import org.jboss.arquillian.test.spi.event.suite.SuiteEvent;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

/**
 * TestContextHandler
 * 
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @version $Revision: $
 */
public class TestContextHandler
{
   @Inject
   private Instance<SuiteContext> suiteContextInstance;

   @Inject
   private Instance<ClassContext> classContextInstance;

   @Inject
   private Instance<TestContext> testContextInstance;

   @Inject
   @ClassScoped
   private InstanceProducer<TestClass> testClassProducer;

   public void createSuiteContext(@Observes(precedence = 100) EventContext<SuiteEvent> context)
   {
      SuiteContext suiteContext = this.suiteContextInstance.get();
      try
      {
         suiteContext.activate();
         context.proceed();
      }
      finally
      {
         suiteContext.deactivate();
         if (AfterSuite.class.isAssignableFrom(context.getEvent().getClass()))
         {
            suiteContext.destroy();
         }
      }
   }

   public void createClassContext(@Observes(precedence = 100) EventContext<ClassEvent> context)
   {
      ClassContext classContext = this.classContextInstance.get();
      try
      {
         classContext.activate(context.getEvent().getTestClass().getJavaClass());
         testClassProducer.set(context.getEvent().getTestClass());
         context.proceed();
      }
      finally
      {
         classContext.deactivate();
         if (AfterClass.class.isAssignableFrom(context.getEvent().getClass()))
         {
            classContext.destroy(context.getEvent().getTestClass().getJavaClass());
         }
      }
   }

   public void createTestContext(@Observes(precedence = 100) EventContext<TestEvent> context)
   {
      TestContext testContext = this.testContextInstance.get();
      try
      {
         testContext.activate(context.getEvent().getTestInstance());
         context.proceed();
      }
      finally
      {
         testContext.deactivate();
         if (After.class.isAssignableFrom(context.getEvent().getClass()))
         {
            testContext.destroy(context.getEvent().getTestInstance());
         }
      }
   }
}
