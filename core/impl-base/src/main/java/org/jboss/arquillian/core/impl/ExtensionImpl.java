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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.core.spi.EventPoint;
import org.jboss.arquillian.core.spi.Extension;
import org.jboss.arquillian.core.spi.InjectionPoint;
import org.jboss.arquillian.core.spi.ObserverMethod;
import org.jboss.arquillian.core.spi.Validate;

/**
 * ExtensionImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ExtensionImpl implements Extension
{
   private Object target;
   private List<InjectionPoint> injectionPoints;
   private List<EventPoint> eventPoints;
   private List<ObserverMethod> observers;
   
   //-------------------------------------------------------------------------------------||
   // Public Factory Methods -------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public static ExtensionImpl of(Object target)
   {
      Validate.notNull(target, "Extension must be specified");
      return new ExtensionImpl(
            target, 
            injections(target, Reflections.getFieldInjectionPoints(target.getClass())),
            events(target, Reflections.getEventPoints(target.getClass())),
            observers(target, Reflections.getObserverMethods(target.getClass())));
   }
   
   private static List<ObserverMethod> observers(Object extension, List<Method> observerMethods) 
   {
      List<ObserverMethod> result = new ArrayList<ObserverMethod>();
      for(Method method : observerMethods)
      {
         result.add(ObserverImpl.of(extension, method));
      }
      return result;
   }

   private static List<InjectionPoint> injections(Object extension, List<Field> injectionPoints) 
   {
      List<InjectionPoint> result = new ArrayList<InjectionPoint>();
      for(Field field : injectionPoints)
      {
         result.add(InjectionPointImpl.of(extension, field));
      }
      return result;
   }

   private static List<EventPoint> events(Object extension, List<Field> eventPoints) 
   {
      List<EventPoint> result = new ArrayList<EventPoint>();
      for(Field method : eventPoints)
      {
         result.add(EventPointImpl.of(extension, method));
      }
      return result;
   }

   
   ExtensionImpl(Object target, List<InjectionPoint> injectionPoints, List<EventPoint> eventPoints, List<ObserverMethod> observers)
   {
      this.target = target;
      this.injectionPoints = injectionPoints;
      this.eventPoints = eventPoints;
      this.observers = observers;
   }

   /**
    * @return the target
    */
   public Object getTarget()
   {
      return target;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - Extension -----------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Extension#getFieldInjectionPoints()
    */
   @Override
   public List<InjectionPoint> getInjectionPoints()
   {
      return Collections.unmodifiableList(injectionPoints);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Extension#getEventPoints()
    */
   @Override
   public List<EventPoint> getEventPoints()
   {
      return Collections.unmodifiableList(eventPoints);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Extension#getObservers()
    */
   @Override
   public List<ObserverMethod> getObservers()
   {
      return Collections.unmodifiableList(observers);
   }
}
