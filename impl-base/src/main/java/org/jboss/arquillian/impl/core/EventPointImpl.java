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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.jboss.arquillian.impl.core.spi.EventPoint;
import org.jboss.arquillian.impl.core.spi.InvocationException;
import org.jboss.arquillian.spi.core.Event;

/**
 * EventPointImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EventPointImpl implements EventPoint
{
   private Object target;
   private Field field;

   //-------------------------------------------------------------------------------------||
   // Public Factory Methods -------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public static EventPointImpl of(Object target, Field field)
   {
      return new EventPointImpl(target, field);
   }

   EventPointImpl(Object target, Field field)
   {
      this.target = target;
      this.field = field;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations - EventPoint ----------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Typed#getType()
    */
   @Override
   public Class<?> getType()
   {
      return (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.InjectionPoint#set(org.jboss.arquillian.api.Instance)
    */
   @Override
   public void set(Event<?> value) throws InvocationException
   {
      try
      {
         if(!field.isAccessible())
         {
            field.setAccessible(true);
         }
         field.set(target, value);
      }
      catch (Exception e) 
      {
         throw new InvocationException(e);
      }
   }
}
