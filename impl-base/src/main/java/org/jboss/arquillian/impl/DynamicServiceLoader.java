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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.arquillian.spi.ServiceLoader;

/**
 * ServiceLoader implementation that use META-INF/services/interface files to registered Services.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DynamicServiceLoader implements ServiceLoader
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private static Logger logger = Logger.getLogger(DynamicServiceLoader.class.getName());

   private static final String SERVICES = "META-INF/services";
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - ServiceLoader -------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#all(java.lang.Class)
    */
   public <T> Collection<T> all(Class<T> serviceClass)
   {
      return all(SecurityActions.getThreadContextClassLoader(), serviceClass);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#all(java.lang.ClassLoader, java.lang.Class)
    */
   @Override
   public <T> Collection<T> all(ClassLoader classLoader, Class<T> serviceClass)
   {
      Validate.notNull(classLoader, "ClassLoader must be provided");
      Validate.notNull(serviceClass, "ServiceClass must be provided");
      
      return createInstances(
            serviceClass, 
            load(serviceClass, classLoader));
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#onlyOne(java.lang.Class)
    */
   public <T> T onlyOne(Class<T> serviceClass)
   {
      return onlyOne(SecurityActions.getThreadContextClassLoader(), serviceClass);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#onlyOne(java.lang.ClassLoader, java.lang.Class)
    */
   @Override
   public <T> T onlyOne(ClassLoader classLoader, Class<T> serviceClass)
   {
      Validate.notNull(classLoader, "ClassLoader must be provided");
      Validate.notNull(serviceClass, "ServiceClass must be provided");

      Set<Class<? extends T>> serviceImpls = load(serviceClass, classLoader);
      verifyOnlyOneOrSameImplementation(serviceClass, serviceImpls);
      
      return createInstance(serviceImpls.iterator().next());
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#onlyOne(java.lang.Class, java.lang.Class)
    */
   public <T> T onlyOne(Class<T> serviceClass, Class<? extends T> defaultServiceClass)
   {
      return onlyOne(SecurityActions.getThreadContextClassLoader(), serviceClass, defaultServiceClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ServiceLoader#onlyOne(java.lang.ClassLoader, java.lang.Class, java.lang.Class)
    */
   @Override
   public <T> T onlyOne(ClassLoader classLoader, Class<T> serviceClass, Class<? extends T> defaultServiceClass)
   {
      Validate.notNull(classLoader, "ClassLoader must be provided");
      Validate.notNull(serviceClass, "ServiceClass must be provided");
      Validate.notNull(defaultServiceClass, "DefaultServiceClass must be provided");
      
      Class<? extends T> serviceImplToCreate = defaultServiceClass;
      
      Set<Class<? extends T>> serviceImpls = load(serviceClass, classLoader);
      if(serviceImpls.size() > 0)
      {
         verifySameImplementation(serviceClass, serviceImpls);
         serviceImplToCreate = serviceImpls.iterator().next();
      }
      return createInstance(serviceImplToCreate);
   }
   
   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private <T> void verifyOnlyOneOrSameImplementation(Class<T> serviceClass, Collection<Class<? extends T>> providers)
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
   
   private <T> void verifySameImplementation(Class<T> serviceClass, Collection<Class<? extends T>> providers)
   {
      boolean providersAreTheSame = false;
      Class<?> firstProvider = null;
      for(Class<?> provider : providers)
      {
         if(firstProvider == null)
         {
            // set the class to match
            firstProvider = provider;
            continue;
         }
         if(firstProvider == provider) 
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
   
   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods - Service Loading ------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private <T> Set<Class<? extends T>> load(Class<T> serviceClass, ClassLoader loader) 
   {
      String serviceFile = SERVICES + "/" + serviceClass.getName();

      LinkedHashSet<Class<? extends T>> providers = new LinkedHashSet<Class<? extends T>>();
      try
      {
         Enumeration<URL> enumeration = loader.getResources(serviceFile);
         while (enumeration.hasMoreElements())
         {
            final URL url = enumeration.nextElement();
            final InputStream is = url.openStream();
            BufferedReader reader = null;
            
            try
            {
               reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
               String line = reader.readLine();
               while (null != line)
               {
                  final int comment = line.indexOf('#');
                  if (comment > -1)
                  {
                     line = line.substring(0, comment);
                  }
   
                  line.trim();
   
                  if (line.length() > 0)
                  {
                     try
                     {
                        providers.add(
                              loader.loadClass(line)
                                 .asSubclass(serviceClass));
                     }
                     catch (ClassCastException e)
                     {
                        throw new IllegalStateException("Service " + line + " does not implement expected type "
                              + serviceClass.getName());
                     }
                  }
                  line = reader.readLine();
               }
            }
            finally
            {
               if (reader != null) 
               {
                  reader.close();
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not load services for " + serviceClass.getName(), e);
      }
      return providers;
   }
   
   /**
    * Create a new instance of the found Service. <br/>
    * 
    * Verifies that the found ServiceImpl implements Service.
    * 
    * @param <T>
    * @param serviceType The Service interface
    * @param className The name of the implementation class
    * @param loader The ClassLoader to load the ServiceImpl from
    * @return A new instance of the ServiceImpl
    * @throws Exception If problems creating a new instance
    */
   private <T> T createInstance(Class<? extends T> serviceImplClass)
   {
      try
      {
         Constructor<? extends T> constructor = SecurityActions.getConstructor(serviceImplClass);
         if (!constructor.isAccessible())
         {
            constructor.setAccessible(true);
         }
         return constructor.newInstance();
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not create a new instance of Service implementation " + serviceImplClass.getName(), e);
      }
   }
   
   private <T> Set<T> createInstances(Class<T> serviceType, Set<Class<? extends T>> providers)
   {
      Set<T> providerImpls = new LinkedHashSet<T>();
      for(Class<? extends T> serviceClass: providers)
      {
         providerImpls.add(createInstance(serviceClass));
      }
      return providerImpls;
   }
}
