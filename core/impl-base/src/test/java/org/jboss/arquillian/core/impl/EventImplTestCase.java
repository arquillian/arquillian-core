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


import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.junit.Assert;
import org.junit.Test;


/**
 * EventImplTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EventImplTestCase
{
   @Test
   public void shouldBeAbleToFireEvent() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .extensions(TestObserver.class).create();
      
      EventImpl<Object> event = EventImpl.of(Object.class, manager);
      
      Object testObject = new Object();
      event.fire(testObject);
      
      TestObserver observer = manager.getExtension(TestObserver.class);
      Assert.assertTrue(observer.wasCalled);
      Assert.assertEquals(
            "Verify same object was observed", 
            testObject, observer.getObject());
   }
   
   private static class TestObserver 
   {
      private boolean wasCalled = false;
      private Object object;
      
      @SuppressWarnings("unused")
      public void shouldBeCalled(@Observes Object object)
      {
         Assert.assertNotNull(object);
         this.object = object;
         wasCalled = true;
      }
      
      public Object getObject()
      {
         return object;
      }
   }
}
