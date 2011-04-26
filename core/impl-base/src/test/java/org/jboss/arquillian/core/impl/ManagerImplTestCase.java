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
package org.jboss.arquillian.core.impl;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.core.spi.NonManagedObserver;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.core.test.context.ManagerTest2Context;
import org.jboss.arquillian.core.test.context.ManagerTest2ContextImpl;
import org.jboss.arquillian.core.test.context.ManagerTestContext;
import org.jboss.arquillian.core.test.context.ManagerTestContextImpl;
import org.jboss.arquillian.core.test.context.ManagerTestScoped;
import org.junit.Assert;
import org.junit.Test;


/**
 * ManagerImplTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ManagerImplTestCase
{
   @Test
   public void shouldBeAbleToRegisterContextAndExtensions() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .context(ManagerTestContextImpl.class)
         .extension(TestExtension.class).create();

      ManagerTestContext context = manager.getContext(ManagerTestContext.class);
      try
      {
         context.activate();
         // bind something to the context so our Instance<Object> is resolved
         context.getObjectStore().add(Object.class, new Object());
         
         manager.fire("some string");
         
         Assert.assertTrue(manager.getExtension(TestExtension.class).wasCalled);
         
      } 
      finally
      {
         context.deactivate();
         context.destroy();
      }
   }

   @Test
   public void shouldBindToTheScopedContext() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .context(ManagerTestContextImpl.class)
         .context(ManagerTest2ContextImpl.class).create();

      ManagerTestContext suiteContext = manager.getContext(ManagerTestContext.class);
      ManagerTest2Context classContext = manager.getContext(ManagerTest2Context.class);

      try
      {
         suiteContext.activate();
         classContext.activate("A");
         
         Object testObject = new Object();
         
         manager.bind(ManagerTestScoped.class, Object.class, testObject);
         
         Assert.assertEquals(
               "Verify value was bound to the correct context",
               testObject, 
               suiteContext.getObjectStore().get(Object.class));

         Assert.assertNull(
               "Verify value was not bound to any other context",
               classContext.getObjectStore().get(Object.class));
      }
      finally
      {
         classContext.deactivate();
         classContext.destroy("A");
         suiteContext.deactivate();
         suiteContext.destroy();
      }
   }
   
   @Test
   public void shouldResolveToNullIfNoActiveContexts() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from().create();
      Assert.assertNull(manager.resolve(Object.class));
   }
   
   @Test
   public void shouldResolveToNullContextIfNotFound() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from().create();
      Assert.assertNull(manager.getContext(ManagerTestContext.class));
   }

   @Test
   public void shouldResolveToNullExtensionIfNotFound() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from().create();
      Assert.assertNull(manager.getExtension(ManagerTestContextImpl.class));
   }

   @Test
   public void shouldCallNonManagedObserver() throws Exception
   {
      final String testEvent = "test";
      
      TestNonManagedObserver observer = new TestNonManagedObserver();
      
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from().create();
      manager.fire(testEvent, observer);
      
      Assert.assertNotNull(
            "NonManagedObserver should have been called", 
            TestNonManagedObserver.firedEvent);
      Assert.assertEquals(
            "NonManagedObserver should have received fired event",
            TestNonManagedObserver.firedEvent, testEvent);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnBindWithNoFoundScopedContext() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from().create();
      manager.bind(ManagerTestScoped.class, Object.class, new Object());
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnBindWithNonActiveScopedContext() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .context(ManagerTestContextImpl.class).create();

      manager.bind(ManagerTestScoped.class, Object.class, new Object());
   }
   
   private static class TestExtension 
   {
      private boolean wasCalled = false;
      
      @Inject
      private Instance<Object> value;
      
      @SuppressWarnings("unused")
      public void on(@Observes String object)
      {
         Assert.assertNotNull("Verify event is not null", object);
         Assert.assertNotNull("Verify InjectionPoint is not null", value);
         Assert.assertNotNull("Verify InjectionPoint value is not null", value.get());
         wasCalled = true;
      }
   }
   
   private static class TestNonManagedObserver implements NonManagedObserver<String>
   {
      private static String firedEvent = null;
      
      @Inject
      private Instance<ApplicationContext> applicationContext;
      
      @Override
      public void fired(String event)
      {
         firedEvent = event;
         if(applicationContext == null)
         {
            throw new IllegalStateException("ApplicationContext should have been injected, but was null");
         }
      }
   }
}
