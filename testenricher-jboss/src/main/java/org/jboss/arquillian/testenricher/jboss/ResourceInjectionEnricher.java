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
package org.jboss.arquillian.testenricher.jboss;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Resource;
import javax.naming.InitialContext;

import org.jboss.arquillian.spi.TestEnricher;

/**
 * ResourceInjectionEnricher
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ResourceInjectionEnricher implements TestEnricher
{
   private static final String RESOURCE_LOOKUP_PREFIX = "java:/comp/env";
   private static final String ANNOTATION_NAME = "javax.annotation.Resource";
   
   @Override
   public void enrich(Object testCase)
   {
      if(SecurityActions.isClassPresent(ANNOTATION_NAME)) 
      {
         injectClass(testCase);
      }
   }

   protected void injectClass(Object testCase) 
   {
      try 
      {
         @SuppressWarnings("unchecked")
         Class<? extends Annotation> resourceAnnotation = (Class<? extends Annotation>)SecurityActions.getThreadContextClassLoader().loadClass(ANNOTATION_NAME);
         
         List<Field> annotatedFields = SecurityActions.getFieldsWithAnnotation(
               testCase.getClass(), 
               resourceAnnotation);
         
         for(Field field : annotatedFields) 
         {
            Object ejb = lookup(getResourceName(field));
            field.set(testCase, ejb);
         }
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not inject members", e);
      }
   }

   protected Object lookup(String jndiName) throws Exception 
   {
      // TODO: figure out test context ? 
      InitialContext context = new InitialContext();
      return context.lookup(jndiName);
   }
   
   protected String getResourceName(Field field)
   {
      Resource resource = field.getAnnotation(Resource.class);
      String mappedName = resource.mappedName();
      if (!mappedName.equals(""))
      {
         return mappedName;
      }
      String name = resource.name();
      if (!name.equals(""))
      {
         return RESOURCE_LOOKUP_PREFIX + "/" + name;
      }
      String propertyName = field.getName();
      String className = field.getDeclaringClass().getName();
      return RESOURCE_LOOKUP_PREFIX + "/" + className + "/" + propertyName;
   }
}
