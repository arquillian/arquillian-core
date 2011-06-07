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
package org.jboss.arquillian.core.impl.loadable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.arquillian.core.spi.ExtensionLoader;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.Validate;

/**
 * ServiceLoader implementation that use META-INF/services/interface files to registered Services.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JavaSPIExtensionLoader implements ExtensionLoader
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private static final String SERVICES = "META-INF/services";
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - ExtensionLoader -----------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Override
   public Collection<LoadableExtension> load()
   {
      return all(JavaSPIExtensionLoader.class.getClassLoader(), LoadableExtension.class);
   }
   
   //-------------------------------------------------------------------------------------||
   // General JDK SPI Loader -------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public <T> Collection<T> all(ClassLoader classLoader, Class<T> serviceClass)
   {
      Validate.notNull(classLoader, "ClassLoader must be provided");
      Validate.notNull(serviceClass, "ServiceClass must be provided");
      
      return createInstances(
            serviceClass, 
            load(serviceClass, classLoader));
   }
   
   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods - Service Loading ------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private <T> Set<Class<? extends T>> load(Class<T> serviceClass, ClassLoader loader) 
   {
      String serviceFile = SERVICES + "/" + serviceClass.getName();

      LinkedHashSet<Class<? extends T>> providers = new LinkedHashSet<Class<? extends T>>();      
      LinkedHashSet<Class<? extends T>> vetoedProviders = new LinkedHashSet<Class<? extends T>>();      
      
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
                  line = skipCommentAndTrim(line);
   
                  if (line.length() > 0)
                  {
                     try
                     {
                        boolean mustBeVetoed = line.startsWith("!");
                        if (mustBeVetoed)
                        {
                           line = line.substring(1);
                        }
                        
                        Class<? extends T> provider = loader.loadClass(line).asSubclass(serviceClass);
                    
                        if (mustBeVetoed) {
                           vetoedProviders.add(provider);
                        }
                        
                        if (vetoedProviders.contains(provider)) {
                           providers.remove(provider);
                        } else {
                           providers.add(provider);
                        }
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

   private String skipCommentAndTrim(String line)
   {
      final int comment = line.indexOf('#');
      if (comment > -1)
      {
         line = line.substring(0, comment);
      }
  
      line = line.trim();
      return line;
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
