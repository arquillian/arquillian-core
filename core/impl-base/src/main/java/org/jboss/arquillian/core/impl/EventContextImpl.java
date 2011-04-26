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
package org.jboss.arquillian.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.InvocationException;
import org.jboss.arquillian.core.spi.NonManagedObserver;
import org.jboss.arquillian.core.spi.ObserverMethod;
import org.jboss.arquillian.core.spi.Validate;

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
   private NonManagedObserver<T> nonManagedObserver;
   
   private T event;
   
   private int currentInterceptor = 0;
   
   /**
    * Create a new EventContext that will process all interceptors, observers and the non managed observer for a given event.
    * 
    * @param manager The manager instance to operate on
    * @param interceptors List of interceptor observers, @Observers of EventContext<T>
    * @param observers List of Observers, @Observes T
    * @param nonManagedObserver a NonManagedObserver of type T
    * @param event The event
    * @throws IllegalArgumentException if Manager is null
    * @throws IllegalArgumentException if Event is null 
    */
   public EventContextImpl(ManagerImpl manager, List<ObserverMethod> interceptors, List<ObserverMethod> observers, NonManagedObserver<T> nonManagedObserver, T event)
   {
      Validate.notNull(manager, "Manager must be specified");
      Validate.notNull(event, "Event must be specified");
      
      this.manager = manager;
      this.interceptors = interceptors == null ? new ArrayList<ObserverMethod>():interceptors;
      this.observers = observers == null ? new ArrayList<ObserverMethod>():observers;
      this.nonManagedObserver = nonManagedObserver;
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
         invokeNonManagedObserver();
      }
      else
      {
         ObserverMethod interceptor = interceptors.get(currentInterceptor++);
         manager.debug(interceptor, true);
         interceptor.invoke(manager, this);
      }
   }
   
   private void invokeObservers()
   {
      for(ObserverMethod observer : observers)
      {
         try
         {
            manager.debug(observer, false);
            observer.invoke(manager, event);
         } 
         catch (InvocationException e) 
         {
            Throwable cause = e.getCause();
            if(manager.isExceptionHandled(cause))
            {
               UncheckedThrow.throwUnchecked(cause);
            }
            else
            {
               manager.fireException(cause);
            }
         }
      }
   }
   
   private void invokeNonManagedObserver()
   {
      if(this.nonManagedObserver != null)
      {
         manager.inject(nonManagedObserver);
         nonManagedObserver.fired(getEvent());
      }
   }
}
