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
package org.jboss.arquillian.core.impl.context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.core.spi.context.ObjectStore;
import org.jboss.arquillian.core.test.context.ManagerTest2Context;
import org.jboss.arquillian.core.test.context.ManagerTest2ContextImpl;
import org.jboss.arquillian.core.test.context.ManagerTestContext;
import org.jboss.arquillian.core.test.context.ManagerTestContextImpl;
import org.junit.Assert;
import org.junit.Test;


/**
 * ContextActivationTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContextActivationTestCase
{
   @Test
   public void shouldBeAbleToReceiveObjectAfterReActivation()
   {
      ManagerTestContext context = new ManagerTestContextImpl();

      try
      {
         Assert.assertFalse(context.isActive());
         
         context.activate();
         Assert.assertTrue(context.isActive());
         
         ObjectStore store = context.getObjectStore();
         store.add(Boolean.class, true);
         
         Assert.assertEquals(
               "Verify that we can get objects from a active context",
               new Boolean(true), 
               store.get(Boolean.class));
         
         context.deactivate();
         Assert.assertFalse(context.isActive());
         
         try
         {
            context.getObjectStore();
            Assert.fail("Trying to get ObjectStore outside active context should have thrown Exception");
         }
         catch (Exception e) {
         }
   
         context.activate();
         store = context.getObjectStore();
   
         Assert.assertEquals(
               "Verify that we can get objects from a active context",
               new Boolean(true), 
               store.get(Boolean.class));
      }
      finally
      {
         context.deactivate();
         context.destroy();
      }
      
   }
   
   @Test
   public void shouldNotBeAbleToReadFromDifferentThread() throws Exception
   {
      final CountDownLatch latch = new CountDownLatch(1);
      final ManagerTestContext context = new ManagerTestContextImpl();
      try
      {
         context.activate();
         context.getObjectStore().add(Object.class, new Object());
         
         Thread thread = new Thread() 
         {
            public void run() 
            {
               Assert.assertFalse(context.isActive());
               
               latch.countDown();
            };
         };
         thread.start();
         
         if(!latch.await(1, TimeUnit.SECONDS))
         {
            Assert.fail("Thread never called?");
         }
      }
      finally
      {
         context.deactivate();
         context.destroy();
      }
   }
   
   @Test
   public void shouldBeAbleToStackContextOfSameType()
   {
      ManagerTest2Context context = new ManagerTest2ContextImpl();      
      
      try
      {
         context.activate("PARENT");
         context.getObjectStore().add(String.class, "test");
         
         try
         {
            context.activate("CHILD");
            Assert.assertNull(
                  "Should not be able to read from previously stacked context", 
                  context.getObjectStore().get(String.class));
            
         }
         finally
         {
            context.deactivate();
         }
         
         Assert.assertTrue(
               "Outer Context should still be active", 
               context.isActive());
      } 
      finally 
      {
         context.deactivate();
         context.clearAll();
      }
   }
}
