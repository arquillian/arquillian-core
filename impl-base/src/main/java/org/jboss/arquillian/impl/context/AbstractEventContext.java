/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.impl.context;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.impl.event.EventManager;
import org.jboss.arquillian.impl.event.MapEventManager;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.Event;
import org.jboss.arquillian.spi.event.suite.EventHandler;


/**
 * AbstractEventContext
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class AbstractEventContext implements Context
{
   private EventManager eventManager;
   
   // TODO: create ObjectStore
   private Map<Class<?>, Object> objectStore;
   
   public AbstractEventContext()
   {
      this.eventManager = new MapEventManager();
      this.objectStore = new HashMap<Class<?>, Object>();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Context#register(java.lang.Class, org.jboss.arquillian.spi.event.suite.EventHandler)
    */
   public <K extends Event> void register(Class<? extends K> eventType, EventHandler<? super K> handler)
   {
      eventManager.register(eventType, handler);
   }
   
   /**
    * @return
    */
   protected EventManager getEventManager() 
   {
      return eventManager;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Context#add(java.lang.Class, java.lang.Object)
    */
   public <B> void add(Class<B> type, B instance) 
   {
      Validate.notNull(type, "Type must be specified");
      Validate.notNull(instance, "Instance must be specified");
      
      objectStore.put(type, instance);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Context#get(java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   public <B> B get(Class<B> type)
   {
      Validate.notNull(type, "Type must be specified");
      
      B instance = (B)objectStore.get(type);
      if(instance == null) 
      {
         Context parentContext = getParentContext();
         if(parentContext != null) 
         {
            instance = parentContext.get(type);
         }
      }
      return instance;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Context#getServiceLoader()
    */
   public ServiceLoader getServiceLoader()
   {
      return get(ServiceLoader.class);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Context#fire(org.jboss.arquillian.spi.event.Event)
    */
   public void fire(Event event)
   {
      Context parent = getParentContext();
      if(parent != null)
      {
         parent.fire(event);
      }
      getEventManager().fire(this, event);
   }
}
