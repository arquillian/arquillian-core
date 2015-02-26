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
package org.jboss.arquillian.test.impl.enricher.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.annotation.ClassInjection;
import org.jboss.arquillian.test.spi.annotation.MethodInjection;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * ArquillianTestEnricher
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianResourceTestEnricher implements TestEnricher
{
   @Inject
   private Instance<ServiceLoader> loader;

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
             List<Annotation> qualifiers = filterAnnotations(Arrays.asList(field.getAnnotations()));
            // null value will throw exception in lookup
            
            ClassInjection classInjectedResource = new ClassInjection()
            {                
                @Override
                public Class<? extends Annotation> annotationType()
                {
                    return ClassInjection.class;
                }
            };

            qualifiers.add(classInjectedResource);
            
            value = lookup(field.getType(), field.getAnnotation(ArquillianResource.class), qualifiers);
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
            List<Annotation> qualifiers = filterAnnotations(Arrays.asList(method.getParameterAnnotations()[i]));

            MethodInjection methodInjectedResource = new MethodInjection()
            {                
                @Override
                public Class<? extends Annotation> annotationType()
                {
                    return MethodInjection.class;
                }
            };

            qualifiers.add(methodInjectedResource);

            values[i] = lookup(method.getParameterTypes()[i], resource, qualifiers);
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
   private Object lookup(Class<?> type, ArquillianResource resource, List<Annotation> qualifiers)
   {
      Collection<ResourceProvider> resourceProviders = loader.get().all(ResourceProvider.class);
      for(ResourceProvider resourceProvider: resourceProviders)
      {
         if(resourceProvider.canProvide(type))
         {
            Object value = resourceProvider.lookup(resource, qualifiers.toArray(new Annotation[0]));
            if(value == null)
            {
               throw new RuntimeException("Provider for type " + type + " returned a null value: " + resourceProvider);
            }
            return value;
         }
      }
      throw new IllegalArgumentException("No ResourceProvider found for type: " + type);
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

   /**
    * @param annotations
    * @return
    */
   private List<Annotation> filterAnnotations(List<Annotation> annotations)
   {
      List<Annotation> filtered = new ArrayList<Annotation>(); 
       
      if(annotations == null)
      {
         return filtered;
      }
      
      for(Annotation annotation : annotations)
      {
         if(annotation.annotationType() != ArquillianResource.class)
         {
            filtered.add(annotation);
         }
      }
      return filtered;
   }
}
