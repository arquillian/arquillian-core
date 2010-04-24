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
package org.jboss.arquillian.impl.context;

import junit.framework.Assert;

import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.ContextLifecycleManager;
import org.jboss.arquillian.impl.context.ProfileBuilder;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;
import org.jboss.arquillian.spi.event.suite.TestEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Test Case to verify context create/restore and event propagation model.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextLifecycleManagerTestCase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock
   private ProfileBuilder profileBuilder;
   
   @Mock
   private EventHandler<SuiteEvent> suiteEventHandler;

   @Mock
   private EventHandler<ClassEvent> classEventHandler;

   @Mock
   private EventHandler<TestEvent> testEventHandler;

   @Test
   public void shouldBeAbleToCreateRestoreSuiteContext() throws Exception 
   {
      ContextLifecycleManager manager = new ContextLifecycleManager(profileBuilder, serviceLoader);

      // Create a new
      SuiteContext context = manager.createRestoreSuiteContext();
      
      Assert.assertNotNull("Context should have been created", context);
      
      // Restore
      SuiteContext context2 = manager.createRestoreSuiteContext();
      
      Assert.assertNotNull("Context should have been created", context2);
      Assert.assertTrue("Restore of context should return same instance", context.equals(context2));
      
      // Destroy and create a new
      manager.destroySuiteContext();
      context2 = manager.createRestoreSuiteContext();

      Assert.assertNotNull("Context should have been created", context2);
      Assert.assertFalse("Restore of context should return same instance", context.equals(context2));
   }
   
   @Test
   public void shouldBeAbleToCreateRestoreClassContext() throws Exception 
   {
      ContextLifecycleManager manager = new ContextLifecycleManager(profileBuilder, serviceLoader);
      
      // Create a new
      manager.createRestoreSuiteContext();
      
      ClassContext context = manager.createRestoreClassContext(getClass());
      Assert.assertNotNull("Context should have been created", context);
      
      ClassContext context2 = manager.createRestoreClassContext(getClass());
      Assert.assertNotNull("Context should have been created", context2);
      Assert.assertTrue("Restore of context should return same instance", context.equals(context2));
      
      manager.destroyClassContext(getClass());
      
      context2 = manager.createRestoreClassContext(getClass());

      Assert.assertNotNull("Context should have been created", context2);
      Assert.assertFalse("Restore of context should return same instance", context.equals(context2));
   }
   
   @Test
   public void shouldBeAbleToCreateRestoreTestContext() throws Exception 
   {
      ContextLifecycleManager manager = new ContextLifecycleManager(profileBuilder, serviceLoader);
      
      // Create a new
      manager.createRestoreSuiteContext();
      manager.createRestoreClassContext(getClass());
      
      TestContext context = manager.createRestoreTestContext(this);
      Assert.assertNotNull("Context should have been created", context);
      
      TestContext context2 = manager.createRestoreTestContext(this);
      Assert.assertNotNull("Context should have been created", context2);
      Assert.assertTrue("Restore of context should return same instance", context.equals(context2));
      
      manager.destroyTestContext(this);
      
      context2 = manager.createRestoreTestContext(this);

      Assert.assertNotNull("Context should have been created", context2);
      Assert.assertFalse("Restore of context should return same instance", context.equals(context2));
   }

   @Test
   public void shouldBeAbleToFireUpwards() throws Exception 
   {
      Before event = new Before(getClass(), getClass().getMethod("shouldBeAbleToFireUpwards"));
      ContextLifecycleManager manager = new ContextLifecycleManager(profileBuilder, serviceLoader);
      
      SuiteContext suiteContext = manager.createRestoreSuiteContext();
      suiteContext.register(Before.class, suiteEventHandler);
      
      ClassContext classContext = manager.createRestoreClassContext(getClass());
      classContext.register(Before.class, classEventHandler);
      
      TestContext testContext = manager.createRestoreTestContext(this);
      testContext.register(Before.class, testEventHandler);
      
      testContext.fire(event);
      
      Mockito.verify(suiteEventHandler, Mockito.times(1)).callback(suiteContext, event);
      Mockito.verify(classEventHandler, Mockito.times(1)).callback(classContext, event);
      Mockito.verify(testEventHandler, Mockito.times(1)).callback(testContext, event);
   }
}
