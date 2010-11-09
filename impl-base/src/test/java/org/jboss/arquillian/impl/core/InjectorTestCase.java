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

import junit.framework.Assert;

import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.ManagerImpl;
import org.jboss.arquillian.impl.core.context.ApplicationContextImpl;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.junit.Test;

/**
 * InjectorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class InjectorTestCase
{

   @Test
   public void shouldBeAbleToDoStaticInjection() throws Exception
   {
      ManagerImpl manager = ManagerBuilder.from()
         .extension(TestObserver.class).create();
      
      manager.getContext(ApplicationContextImpl.class).getObjectStore()
         .add(Object.class, new Object());
      
      manager.fire("test event");
      
      Assert.assertTrue(manager.getExtension(TestObserver.class).wasCalled);
   }
   
   private static class TestObserver 
   {
      private boolean wasCalled;
      
      @Inject
      private Instance<Injector> injectorInstance;
      
      @SuppressWarnings("unused")
      public void on(@Observes String test)
      {
         TestStaticInjected target = new TestStaticInjected();
         injectorInstance.get().inject(target);
         
         target.check();
         
         wasCalled = true;
      }
   }
   
   private static class TestStaticInjected
   {
      @Inject
      private Instance<Object> objectnstance;
      
      public void check() 
      {
         Assert.assertNotNull(objectnstance);
         Assert.assertNotNull(objectnstance.get());
      }
   }
}
