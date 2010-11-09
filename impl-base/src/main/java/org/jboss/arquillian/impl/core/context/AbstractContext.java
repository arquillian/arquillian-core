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
package org.jboss.arquillian.impl.core.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.jboss.arquillian.impl.core.ObjectStore;
import org.jboss.arquillian.impl.core.Validate;
import org.jboss.arquillian.impl.core.spi.context.Context;

/**
 * AbstractContext
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class AbstractContext<T> implements Context, IdBoundContext<T>
{
   private static Logger log = Logger.getLogger("Context");
   
   private ConcurrentHashMap<T, ObjectStore> stores;
   
   private ThreadLocal<ObjectStore> activeStore = new ThreadLocal<ObjectStore>();
   
   public AbstractContext()
   {
      stores = new ConcurrentHashMap<T, ObjectStore>();
   }
   
   @Override
   public void activate(T id) 
   {
      Validate.notNull(id, "ID must be specified");
      if(!isActive())
      {
         activeStore.set(createObjectStore(id));
      }
      else
      {
         log.info("Trying to activate context, but allready active: " + super.getClass().getSimpleName() + " " + id);
      }
   }
   
   @Override
   public void deactivate() 
   {
      if(isActive())
      {
         activeStore.set(null);
      }
      else
      {
         log.info("Trying to deactivate context, but none active: " + super.getClass().getSimpleName());
      }
   }

   @Override
   public boolean isActive()
   {
      return activeStore.get() != null;
   }
   
   @Override
   public void destroy(T id) 
   {
      ObjectStore store = stores.remove(id);
      if(store != null)
      {
         store.clear();
      }
   }
   
   @Override
   public ObjectStore getObjectStore()
   {
      if(isActive())
      {
         return activeStore.get();
      }
      throw new RuntimeException("Context is not active: " + super.getClass().getSimpleName());
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.core.spi.context.Context#clearAll()
    */
   @Override
   public void clearAll()
   {
      synchronized (this)
      {
         if(isActive())
         {
            deactivate();
         }
         for(Map.Entry<T, ObjectStore> entry: stores.entrySet())
         {
            entry.getValue().clear();
         }
         stores.clear();
      }
   }
   
   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   
   private ObjectStore createObjectStore(T id)
   {
      Validate.notNull(id, "ID must be specified");
      ObjectStore store = new ObjectStore();
      ObjectStore previousStore = stores.putIfAbsent(id, store);
      if(previousStore != null)
      {
         return previousStore;
      }
      return store;
   }
}
