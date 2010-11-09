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

import org.jboss.arquillian.impl.core.ExtensionImpl;
import org.jboss.arquillian.impl.core.spi.EventPoint;
import org.jboss.arquillian.impl.core.spi.Extension;
import org.jboss.arquillian.impl.core.spi.InjectionPoint;
import org.jboss.arquillian.impl.core.spi.ObserverMethod;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.junit.Assert;
import org.junit.Test;


/**
 * ExtensionParseTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ExtensionParseTestCase
{
   @Test
   public void shouldBeAbleToReadAndInvokeObserverMethods() throws Exception
   {
      ExtensionWithObservers target = new ExtensionWithObservers();
      Extension extension = ExtensionImpl.of(target);
      
      Assert.assertEquals(
            "Verify correct observer methods were found",
            2, extension.getObservers().size());
      
      for(ObserverMethod observer : extension.getObservers())
      {
         observer.invoke(new String());
      }
      
      Assert.assertTrue(target.methodOneWasCalled);
      Assert.assertTrue(target.methodTwoWasCalled);
   }

   @Test
   public void shouldBeAbleToReadAndInvokeInjectionPoints() throws Exception
   {
      ExtensionWithInjection target = new ExtensionWithInjection();
      Extension extension = ExtensionImpl.of(target);
      
      Assert.assertEquals(
            "Verify correct injection fields were found",
            1, extension.getInjectionPoints().size());
      
      Instance<Object> instance = new DummyInstanceImpl();
      for(InjectionPoint point : extension.getInjectionPoints())
      {
         point.set(instance);
      }
      extension.getObservers().get(0).invoke(new Object());
      
      Assert.assertTrue(target.methodOneWasCalled);
      Assert.assertNotNull(instance.get());
   }
   
   @Test
   public void shouldBeAbleToInjectEventAndFireNewEvent() throws Exception
   {
      ExtensionWithEvent target = new ExtensionWithEvent();
      Extension extension = ExtensionImpl.of(target);
      
      Assert.assertEquals(
            "Verify correct event fields were found",
            1, extension.getEventPoints().size());
      
      Assert.assertEquals(1, extension.getObservers().size());
      
      DummyEventImpl event = new DummyEventImpl();
      for(EventPoint point : extension.getEventPoints())
      {
         point.set(event);
      }
      extension.getObservers().get(0).invoke(new Object());  
      
      Assert.assertTrue(target.methodOneWasCalled);
      Assert.assertEquals(
            "Verify the Extensions Event was firable",
            "some string", event.getString());
   }
   
   private static class DummyInstanceImpl implements InstanceProducer<Object> 
   {
      private Object object;
      
      @Override
      public Object get()
      {
         return object;
      }
      @Override
      public void set(Object value)
      {
         this.object = value;
      }
   }
   
   private static class DummyEventImpl implements Event<String>
   {
      private String object;
      
      @Override
      public void fire(String event)
      {
         this.object = event;
      }

      public String getString()
      {
         return object;
      }
   }

   private static class ExtensionWithObservers 
   {
      private boolean methodOneWasCalled = false;
      private boolean methodTwoWasCalled = false;
      
      @SuppressWarnings("unused")
      public void methodOne(@Observes Object object)
      {
         methodOneWasCalled = true;
      }

      @SuppressWarnings("unused")
      public void methodTwo(@Observes Object object)
      {
         methodTwoWasCalled = true;
      }
   }

   private static class ExtensionWithInjection 
   {
      private boolean methodOneWasCalled = false;

      @Inject
      private InstanceProducer<Object> object;
      
      @SuppressWarnings("unused")
      public void methodOne(@Observes Object event)
      {
         Assert.assertNotNull(object);
         Assert.assertNull(object.get());
         
         object.set(new Object());
         methodOneWasCalled = true;
      }
   }
   
   private static class ExtensionWithEvent 
   {
      private boolean methodOneWasCalled = false;
      
      @Inject
      private Event<String> stringEvent;
      
      @SuppressWarnings("unused")
      public void methodOne(@Observes Object event)
      {
         Assert.assertNotNull(event);
         Assert.assertNotNull(stringEvent);
         stringEvent.fire("some string");
         methodOneWasCalled = true;
      }
   }
}
