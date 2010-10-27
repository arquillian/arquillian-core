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

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.annotation.SuiteScoped;
import org.jboss.arquillian.core.impl.context.ContainerContextImpl;
import org.jboss.arquillian.core.impl.context.SuiteContextImpl;
import org.jboss.arquillian.core.spi.context.ContainerContext;
import org.jboss.arquillian.core.spi.context.SuiteContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * IntegrationTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class IntegrationTestCase
{

   @Test
   public void shouldBeAbleToInjectAndFireEvents() throws Exception
   {
      ManagerImpl manager = ManagerBuilder.from()
         .context(SuiteContextImpl.class)
         .context(ContainerContextImpl.class)
         .extensions(
               TestObserverOne.class, 
               TestObserverTwo.class, 
               TestObserverTree.class).create();
      
      SuiteContext context = manager.getContext(SuiteContext.class);
      try
      {
         context.activate();
         context.getObjectStore().add(Object.class, new Object());
         
         
         manager.fire("some string event");

         Assert.assertTrue(manager.getExtension(TestObserverOne.class).wasCalled);
         Assert.assertTrue(manager.getExtension(TestObserverTwo.class).wasCalled);
         Assert.assertTrue(manager.getExtension(TestObserverTree.class).wasCalled);
         
         Assert.assertNotNull(
               "Verify instance was bound to context",
               context.getObjectStore().get(Object.class));
         
         ContainerContext containerContext = manager.getContext(ContainerContext.class);
         Assert.assertFalse(containerContext.isActive());
         
         containerContext.activate("");
         try
         {
            Assert.assertNotNull(
                  "Should have set a Double in container scope",
                  containerContext.getObjectStore().get(Double.class));
         }
         finally
         {
            containerContext.deactivate();
            containerContext.destroy("");
         }
      }
      finally
      {
         context.deactivate();
         context.destroy();
      }
   }
   
   public static class TestObserverOne 
   {
      private boolean wasCalled = false;
      
      @Inject @SuiteScoped
      private InstanceProducer<Object> object;
      
      @Inject
      private Event<Integer> event;
      
      @Inject 
      private Instance<ContainerContext> context;
      
      public void on(@Observes String name)
      {
         try
         {
            context.get().activate("");
            object.set(new Object());
            event.fire(new Integer(100));
         }
         finally
         {
            context.get().deactivate();
         }
         wasCalled = true;
      }
   }
   
   public static class TestObserverTwo 
   {
      private boolean wasCalled = false;
    
      
      public void on(@Observes Integer integer)
      {
         wasCalled = true;
      }
   }

   public static class TestObserverTree 
   {
      private boolean wasCalled = false;
      
      @Inject @ContainerScoped
      private InstanceProducer<Double> doub;
    
      public void on(@Observes Object integer)
      {
         doub.set(new Double(2.0));
         wasCalled = true;
      }
   }
}
