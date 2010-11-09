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
package org.jboss.arquillian.impl.core;

import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.ManagerImpl;
import org.jboss.arquillian.impl.core.context.ClassContextImpl;
import org.jboss.arquillian.impl.core.context.SuiteContextImpl;
import org.jboss.arquillian.impl.core.spi.context.ClassContext;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
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
      ManagerImpl manager = ManagerBuilder.from()
         .context(SuiteContextImpl.class)
         .extension(TestExtension.class).create();

      SuiteContext context = manager.getContext(SuiteContext.class);
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
      ManagerImpl manager = ManagerBuilder.from()
         .context(SuiteContextImpl.class)
         .context(ClassContextImpl.class).create();

      SuiteContext suiteContext = manager.getContext(SuiteContext.class);
      ClassContext classContext = manager.getContext(ClassContext.class);

      try
      {
         suiteContext.activate();
         classContext.activate(this.getClass());
         
         Object testObject = new Object();
         
         manager.bind(SuiteScoped.class, Object.class, testObject);
         
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
         classContext.destroy(this.getClass());
         suiteContext.deactivate();
         suiteContext.destroy();
      }
   }
   
   @Test
   public void shouldResolveToNullIfNoActiveContexts() throws Exception
   {
      ManagerImpl manager = ManagerBuilder.from().create();
      Assert.assertNull(manager.resolve(Object.class));
   }
   
   @Test
   public void shouldResolveToNullContextIfNotFound() throws Exception
   {
      ManagerImpl manager = ManagerBuilder.from().create();
      Assert.assertNull(manager.getContext(SuiteContextImpl.class));
   }

   @Test
   public void shouldResolveToNullExtensionIfNotFound() throws Exception
   {
      ManagerImpl manager = ManagerBuilder.from().create();
      Assert.assertNull(manager.getExtension(SuiteContextImpl.class));
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnBindWithNoFoundScopedContext() throws Exception
   {
      ManagerImpl manager = ManagerBuilder.from().create();
      manager.bind(SuiteScoped.class, Object.class, new Object());
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnBindWithNonActiveScopedContext() throws Exception
   {
      ManagerImpl manager = ManagerBuilder.from()
         .context(SuiteContextImpl.class).create();

      manager.bind(SuiteScoped.class, Object.class, new Object());
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
}
