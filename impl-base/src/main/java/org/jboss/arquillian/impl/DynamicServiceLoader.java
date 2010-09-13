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
package org.jboss.arquillian.impl;

import java.util.Collection;
import java.util.logging.Logger;

import org.jboss.arquillian.spi.util.ServiceLoader;

/**
 * DynamicServiceLoader
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DynamicServiceLoader implements org.jboss.arquillian.spi.ServiceLoader
{
   private static Logger logger = Logger.getLogger(DynamicServiceLoader.class.getName());
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#all(java.lang.Class)
    */
   public <T> Collection<T> all(Class<T> serviceClass)
   {
      Validate.notNull(serviceClass, "ServiceClass must be provided");
      return ServiceLoader.load(serviceClass).getProviders();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#onlyOne(java.lang.Class)
    */
   public <T> T onlyOne(Class<T> serviceClass)
   {
      Validate.notNull(serviceClass, "ServiceClass must be provided");
      Collection<T> providers = ServiceLoader.load(serviceClass).getProviders();
      verifyOnlyOneOrSameImplementation(serviceClass, providers);

      return providers.iterator().next();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#onlyOne(java.lang.Class, java.lang.Class)
    */
   public <T> T onlyOne(Class<T> serviceClass, Class<? extends T> defaultServiceClass)
   {
      Validate.notNull(serviceClass, "ServiceClass must be provided");
      Validate.notNull(defaultServiceClass, "DefaultServiceImpl must be provided");
      
      ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClass); 
      Collection<T> providers = serviceLoader.getProviders();
      try
      { 
         verifyOnlyOneOrSameImplementation(serviceClass, providers);
         return providers.iterator().next();
      }
      catch (IllegalStateException e) 
      {
         try
         {
            return serviceLoader.createInstance(defaultServiceClass.getName());
         }
         catch (Exception e2) 
         {
            throw new IllegalStateException("Could not create default service instance", e2);
         }
      }
   }
   
   private void verifyOnlyOneOrSameImplementation(Class<?> serviceClass, Collection<?> providers)
   {
      if(providers == null || providers.size() == 0)
      {
         throw new IllegalStateException(
               "No implementation found for " + 
               serviceClass.getName() + ", please check your classpath");
      }
      if(providers.size() > 1)
      {
         // verify that they all point to the same implementation, if not throw exception
         verifySameImplementation(serviceClass, providers);
      }
   }
   
   private void verifySameImplementation(Class<?> serviceClass, Collection<?> providers)
   {
      boolean providersAreTheSame = false;
      Class<?> firstProvider = null;
      for(Object provider : providers)
      {
         if(firstProvider == null)
         {
            // set the class to match
            firstProvider = provider.getClass();
            continue;
         }
         if(firstProvider == provider.getClass()) 
         {
            providersAreTheSame = true;
         } 
         else
         {
            throw new IllegalStateException(
                  "More then one implementation found for " 
                  + serviceClass.getName() + ", " +
                  "please check your classpath. The found implementations are " + toClassString(providers));            
         }
      }
      if(providersAreTheSame)
      {
         logger.warning(
               "More then one reference to the same implementation was found for " + 
               serviceClass.getName() + ", please verify you classpath");
      }
   }
   
   private String toClassString(Collection<?> providers)
   {
      StringBuilder sb = new StringBuilder();
      for(Object provider : providers)
      {
         sb.append(provider.getClass().getName()).append(", ");
      }
      return sb.subSequence(0, sb.length()-2).toString();
   }
}
