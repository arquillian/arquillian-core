/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.impl.bootstrap;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.context.ClassContextImpl;
import org.jboss.arquillian.impl.core.context.SuiteContextImpl;
import org.jboss.arquillian.impl.core.context.TestContextImpl;
import org.jboss.arquillian.impl.core.spi.Manager;
import org.jboss.arquillian.impl.core.spi.context.ClassContext;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.impl.core.spi.context.TestContext;
import org.jboss.arquillian.spi.event.suite.After;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.junit.Test;


/**
 * TestContextControllerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestContextControllerTestCase
{
   @Test
   public void shouldControlTheContexts() throws Exception
   {
      Manager manager = ManagerBuilder.from()
         .context(SuiteContextImpl.class)
         .context(ClassContextImpl.class)
         .context(TestContextImpl.class)
         .extensions(ContextActivator.class, ContextDeActivator.class).create();
      
      // no active contexts 
      verify(false, false, false, manager);

      manager.fire(new BeforeSuite());

      // suite context active 
      verify(true, false, false, manager);

      manager.fire(new BeforeClass(TestContextControllerTestCase.class));

      // suite and class context active 
      verify(true, true, false, manager);

      manager.fire(new Before(this, getTestMethod()));

      // suite, class and test context active 
      verify(true, true, true, manager);

      manager.fire(new After(this, getTestMethod()));
      
      // suite and class context active 
      verify(true, true, false, manager);

      manager.fire(new AfterClass(TestContextControllerTestCase.class));

      // suite context active 
      verify(true, false, false, manager);
      
      manager.fire(new AfterSuite());
      
      // no active contexts 
      verify(false, false, false, manager);
   }

   private void verify(boolean suite, boolean clazz, boolean test, Manager manager)
   {
      Assert.assertEquals(suite, manager.getContext(SuiteContext.class).isActive());
      Assert.assertEquals(clazz, manager.getContext(ClassContext.class).isActive());
      Assert.assertEquals(test, manager.getContext(TestContext.class).isActive());
   }

   private Method getTestMethod() throws Exception
   {
      return this.getClass().getMethod("shouldControlTheContexts");
   }
}
