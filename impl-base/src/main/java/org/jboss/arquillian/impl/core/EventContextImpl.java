/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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

import java.util.List;

import org.jboss.arquillian.impl.core.spi.EventContext;
import org.jboss.arquillian.impl.core.spi.InvocationException;
import org.jboss.arquillian.impl.core.spi.ObserverMethod;

/**
 * EventContextImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EventContextImpl<T> implements EventContext<T>
{
   private ManagerImpl manager;
   private List<ObserverMethod> interceptors;
   private List<ObserverMethod> observers;
   private T event;
   
   private int currentInterceptor = 0;
   
   public EventContextImpl(ManagerImpl manager, List<ObserverMethod> interceptors, List<ObserverMethod> observers, T event)
   {
      this.manager = manager;
      this.interceptors = interceptors;
      this.observers = observers;
      this.event = event;
   }

   @Override
   public T getEvent()
   {
      return event;
   }
   
   @Override
   public void proceed()
   {

      if(currentInterceptor == interceptors.size())
      {
         invokeObservers();
      }
      else
      {
         ObserverMethod interceptor = interceptors.get(currentInterceptor++);
         interceptor.invoke(manager, this);
      }
   }
   
   private void invokeObservers()
   {
      for(ObserverMethod observer : observers)
      {
         try
         {
            observer.invoke(manager, event);
         } 
         catch (InvocationException e) 
         {
            UncheckedThrow.throwUnchecked(e.getCause());
         }
      }
   }
}
