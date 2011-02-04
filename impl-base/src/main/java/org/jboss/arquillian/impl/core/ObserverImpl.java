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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.arquillian.impl.core.spi.InvocationException;
import org.jboss.arquillian.impl.core.spi.ObserverMethod;
import org.jboss.arquillian.spi.core.annotation.Observes;

/**
 * ObjectObserver
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ObserverImpl implements ObserverMethod, Comparable<ObserverMethod>
{
   private Object target;
   private Method method;
 
   //-------------------------------------------------------------------------------------||
   // Public Factory Methods -------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public static ObserverImpl of(Object extension, Method observerMethod)
   {
      return new ObserverImpl(extension, observerMethod);
   }
   
   ObserverImpl(Object target, Method method)
   {
      this.target = target;
      this.method = method;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - ObserverMethod ------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Typed#getType()
    */
   @Override
   public Class<?> getType()
   {
      return method.getParameterTypes()[0];
   }

   /**
    * @return the method
    */
   @Override
   public Method getMethod()
   {
      return method;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.ObserverMethod#invoke(java.lang.Object)
    */
   @Override
   public void invoke(Object event) 
   {
      try
      {
         method.invoke(target, event);
      }
      catch (Exception e) 
      {
         if(e instanceof InvocationTargetException)
         {
            throw new InvocationException(((InvocationTargetException)e).getTargetException());
         }
         throw new InvocationException(e.getCause());
      }
   }

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(ObserverMethod o)
   {
      if(o == null)
      {
         return 1;
      }
      Integer a = getPresedence(getMethod());
      Integer b = getPresedence(o.getMethod());
      return b.compareTo(a);
   }
   
   private Integer getPresedence(Method method)
   {
      for(Annotation[] annotations : method.getParameterAnnotations())
      {
         for(Annotation annotation : annotations)
         {
            if(annotation.annotationType() == Observes.class)
            {
               return ((Observes)annotation).precedence();
            }
         }
      }
      return 0;
   }
}
