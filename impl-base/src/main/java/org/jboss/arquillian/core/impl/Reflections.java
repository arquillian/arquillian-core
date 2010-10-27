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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.annotation.Scope;

/**
 * Reflections
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
final class Reflections
{
   private Reflections() {}
   
   public static List<Method> getObserverMethods(Class<?> clazz)
   {
      List<Method> observerMethods = new ArrayList<Method>();
      for(Method method : clazz.getMethods())
      {
         if(isObserverMethod(method))
         {
            observerMethods.add(method);
         }
      }
      return observerMethods;
   }

   /**
    * @param class1
    * @return
    */
   public static List<Field> getFieldInjectionPoints(Class<?> clazz)
   {
      List<Field> injectionPoints = new ArrayList<Field>();
      for(Field field : clazz.getDeclaredFields())
      {
         if(isInjectionPoint(field))
         {
            injectionPoints.add(field);
         }
      }
      return injectionPoints;
   }

   /**
    * @param class1
    * @return
    */
   public static List<Field> getEventPoints(Class<?> clazz)
   {
      List<Field> eventPoints = new ArrayList<Field>();
      for(Field field : clazz.getDeclaredFields())
      {
         if(isEventPoint(field))
         {
            eventPoints.add(field);
         }
      }
      return eventPoints;
   }

   public static Class<? extends Annotation> getScope(Field field)
   {
      for(Annotation annotation : field.getAnnotations())
      {
         Class<? extends Annotation> annotationType = annotation.annotationType();
         if(annotationType.isAnnotationPresent(Scope.class))
         {
            return annotationType;
         }
      }
      return null;
   }
   
   public static <T> T createInstance(Class<T> clazz) throws Exception
   {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      if(!constructor.isAccessible())
      {
         constructor.setAccessible(true);
      }
      return constructor.newInstance();
   }
   
   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @param field
    * @return
    */
   private static boolean isEventPoint(Field field)
   {
      return field.isAnnotationPresent(Inject.class) && field.getType() == Event.class;
   }

   /**
    * @param field
    * @return
    */
   private static boolean isInjectionPoint(Field field)
   {
      return field.isAnnotationPresent(Inject.class) && (field.getType() == Instance.class || field.getType() == InstanceProducer.class);
   }

   /**
    * @param method
    * @return
    */
   private static boolean isObserverMethod(Method method)
   {
      if(method.getParameterTypes().length != 1)
      {
         return false;
      }
      if(containsAnnotation(Observes.class, method.getParameterAnnotations()[0]))
      {
         return true;
      }
      return false;
   }

   /**
    * @param annotations
    * @return
    */
   private static boolean containsAnnotation(Class<? extends Annotation> match, Annotation[] annotations)
   {
      for(Annotation annotation : annotations)
      {
         if(annotation.annotationType() == match)
         {
            return true;
         }
      }
      return false;
   }

   
}
