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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * ArquillianTestEnricher
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianResourceTestEnricher implements TestEnricher
{
   private static Map<Class<?>, ResourceProvider> providers = new HashMap<Class<?>, ResourceProvider>();
   static
   {
      providers.put(URL.class, new URLResourceProvider());
      providers.put(URI.class, new URIResourceProvider());
      providers.put(InitialContext.class, new InitialContextProvider());
      providers.put(Context.class, new InitialContextProvider());
      providers.put(Deployer.class, new DeployerProvider());
      providers.put(ContainerController.class, new ContainerControllerProvider());
   }
   
   @Inject
   private Instance<Injector> injector;
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestEnricher#enrich(java.lang.Object)
    */
   public void enrich(Object testCase)
   {
      for(Field field : SecurityActions.getFieldsWithAnnotation(testCase.getClass(), ArquillianResource.class))
      {
         Object value = null;
         try
         {
            // null value will throw exception in lookup
            value = lookup(field.getType(), field.getAnnotation(ArquillianResource.class));
         }
         catch (Exception e) 
         {
            throw new RuntimeException("Could not lookup value for field " + field, e);
         }
         try
         {
            if(!field.isAccessible())
            {
               field.setAccessible(true);            
            }
               field.set(testCase, value);
            }
            catch (Exception e) 
            {
               throw new RuntimeException("Could not set value on field " + field + " using " + value);
            }
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestEnricher#resolve(java.lang.reflect.Method)
    */
   public Object[] resolve(Method method)
   {
      Object[] values = new Object[method.getParameterTypes().length];
      Class<?>[] parameterTypes = method.getParameterTypes();
      for(int i = 0; i < parameterTypes.length; i++)
      {
         ArquillianResource resource = getResourceAnnotation(method.getParameterAnnotations()[i]);
         if(resource != null)
         {
            values[i] = lookup(method.getParameterTypes()[i], resource);   
         }
          
      }
      return values;
   }

   /**
    * 
    * @param type
    * @param resource
    * @return
    * @throws IllegalArgumentException If no ResourceProvider found for Type
    * @throws RuntimeException If ResourceProvider return null 
    */
   private Object lookup(Class<?> type, ArquillianResource resource)
   {
      ResourceProvider provider = providers.get(type);
      if(provider == null)
      {
         throw new IllegalArgumentException("No ResourceProvider found for type: " + type);
      }
      injector.get().inject(provider);
      
      Object value = provider.lookup(resource);
      if(value == null)
      {
         throw new RuntimeException("Provider for type " + type + " returned a null value: " + provider);
      }
      return value;
   }

   private ArquillianResource getResourceAnnotation(Annotation[] annotations)
   {
      for(Annotation annotation : annotations)
      {
         if(annotation.annotationType() == ArquillianResource.class)
         {
            return (ArquillianResource)annotation;
         }
      }
      return null;
   }
   
}
