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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * MixedServiceLoader
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServiceRegistryLoader implements ServiceLoader
{
   private Injector injector;
   private ServiceRegistry registry;
   private ConcurrentMap<Class<?>, Collection<Object>> serviceInstanceRegistry;
   
   public ServiceRegistryLoader(Injector injector, ServiceRegistry registry)
   {
      this.injector = injector;
      this.registry = registry;
      this.serviceInstanceRegistry = new ConcurrentHashMap<Class<?>, Collection<Object>>();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.core.spi.ServiceLoader#all(java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> Collection<T> all(Class<T> serviceClass)
   {
      if(serviceInstanceRegistry.containsKey(serviceClass))
      {
         return (Collection<T>)serviceInstanceRegistry.get(serviceClass);
      }
      
      List<T> serviceImpls = new ArrayList<T>();
      Set<Class<? extends T>> serviceImplClasses = registry.getServiceImpls(serviceClass);
      for(Class<? extends T> serviceImplClass : serviceImplClasses)
      {
         T serviceImpl = createServiceInstance(serviceImplClass);
         injector.inject(serviceImpl);
         serviceImpls.add(serviceImpl);
      }
      //Collection<T> previouslyRegistrered = (Collection<T>)serviceInstanceRegistry.putIfAbsent(serviceClass, (Collection<Object>)serviceImpls);
      //return previouslyRegistrered != null ? previouslyRegistrered:serviceImpls;
      return serviceImpls;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.core.spi.ServiceLoader#onlyOne(java.lang.Class)
    */
   @Override
   public <T> T onlyOne(Class<T> serviceClass)
   {
      Collection<T> all = all(serviceClass);
      if(all.size() == 1)
      {
         return all.iterator().next();
      }
      if(all.size() > 1)
      {
         throw new RuntimeException("Multiple service implementations found for " + serviceClass + ". " + toClassString(all));
      }
      return null;
   }


   /* (non-Javadoc)
    * @see org.jboss.arquillian.core.spi.ServiceLoader#onlyOne(java.lang.Class, java.lang.Class)
    */
   @Override
   public <T> T onlyOne(Class<T> serviceClass, Class<? extends T> defaultServiceClass)
   {
      T one = null;
      try
      { 
         one = onlyOne(serviceClass);
      }
      catch (Exception e) { }
      
      if(one == null)
      {
         one = createServiceInstance(defaultServiceClass);
      }
      return one;
   }


   private <T> T createServiceInstance(Class<T> service)
   {
      return SecurityActions.newInstance(
            service, 
            new Class<?>[]{}, 
            new Object[]{});
   }
   
   private <T> String toClassString(Collection<T> providers)
   {
      StringBuilder sb = new StringBuilder();
      for(Object provider : providers)
      {
         sb.append(provider.getClass().getName()).append(", ");
      }
      return sb.subSequence(0, sb.length()-2).toString();
   }

}
