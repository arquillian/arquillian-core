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
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.core.test.context.ManagerTestContext;
import org.jboss.arquillian.core.test.context.ManagerTestContextImpl;
import org.jboss.arquillian.core.test.context.ManagerTestScoped;
import org.junit.Assert;
import org.junit.Test;


/**
 * InstanceImplTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class InstanceImplTestCase
{
   @Test
   public void shouldBeAbleToLookupInContext() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .context(ManagerTestContextImpl.class).create();

      Object testObject = new Object();
      ManagerTestContext context = manager.getContext(ManagerTestContext.class);
      try
      {
         context.activate();
         context.getObjectStore().add(Object.class, testObject);
         
         Instance<Object> instance = InstanceImpl.of(Object.class, ManagerTestScoped.class, manager);
         
         Assert.assertEquals(
               "Verify expected object was returned",
               testObject, instance.get());
      } 
      finally
      {
         context.deactivate();
         context.destroy();
      }
   }

   @Test
   public void shouldFireEventOnSet() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .context(ManagerTestContextImpl.class)
         .extension(TestObserver.class).create();

      ManagerTestContext context = manager.getContext(ManagerTestContext.class);
      try
      {
         context.activate();
         
         InstanceProducer<Object> instance = InstanceImpl.of(Object.class, ManagerTestScoped.class, manager);
         instance.set(new Object());

         Assert.assertTrue(manager.getExtension(TestObserver.class).wasCalled);
      } 
      finally
      {
         context.deactivate();
         context.destroy();
      }
   }

   @Test(expected = IllegalStateException.class)
   public void shouldThrowExceptionIfTryingToSetAUnScopedInstance() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from().create();
      InstanceProducer<Object> instance = InstanceImpl.of(Object.class, null, manager);
      
      instance.set(new Object());
      Assert.fail("Should have thrown " + IllegalStateException.class);
   }
   
   private static class TestObserver 
   {
      private boolean wasCalled = false;
      
      @SuppressWarnings("unused")
      public void shouldBeCalled(@Observes Object object)
      {
         Assert.assertNotNull(object);
         wasCalled = true;
      }
   }
}
