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
import org.jboss.arquillian.core.spi.Manager;

/**
 * EventImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EventImpl<T> implements Event<T>
{
   private Manager manager;
   private Class<T> type;
   
   //-------------------------------------------------------------------------------------||
   // Public Factory Methods -------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public static <X> EventImpl<X> of(Class<X> type, Manager manager)
   {
      return new EventImpl<X>(type, manager);
   }
   
   EventImpl(Class<T> type, Manager manager)
   {
      this.type = type;
      this.manager = manager;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - Event ---------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Typed#getType()
    */
   //@Override
   public Class<?> getType() 
   {
      return type;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Event#fire(java.lang.Object)
    */
   @Override
   public void fire(T event)
   {
      manager.fire(event);
   }

}
