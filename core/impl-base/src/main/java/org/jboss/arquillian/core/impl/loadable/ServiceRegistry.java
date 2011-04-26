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
package org.jboss.arquillian.core.impl.loadable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * ServiceRegistry
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServiceRegistry
{
   private Injector injector;
   private Map<Class<?>, Set<Class<?>>> registry;
   
   public ServiceRegistry(Injector injector)
   {
      this.registry = new HashMap<Class<?>, Set<Class<?>>>();
      this.injector = injector;
   }
   
   public <T> void addService(Class<T> service, Class<? extends T> serviceImpl)
   {
      synchronized (registry)
      {
         Set<Class<?>> registeredImpls = registry.get(service);
         if(registeredImpls == null)
         {
            registeredImpls = new HashSet<Class<?>>();
         }
         registeredImpls.add(serviceImpl);   
         registry.put(service, registeredImpls);
      }
   }
   
   public <T> Set<Class<? extends T>> getServiceImpls(Class<T> service)
   {
      Set<Class<?>> registeredImpls = registry.get(service);
      Set<Class<? extends T>> typedImpls = new HashSet<Class<? extends T>>();
      if(registeredImpls == null)
      {
         return typedImpls;
      }
      for(Class<?> registeredImpl : registeredImpls)
      {
         typedImpls.add(registeredImpl.asSubclass(service));
      }
      
      return typedImpls;
   }
   
   public void clear()
   {
      registry.clear();
   }
   
   public ServiceLoader getServiceLoader()
   {
      return new ServiceRegistryLoader(injector, this);
   }
}
