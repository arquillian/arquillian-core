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
package org.jboss.arquillian.testenricher.ejb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.spi.TestEnricher;

/**
 * Enricher that provide EJB class and setter method injection. 
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EJBInjectionEnricher implements TestEnricher
{
   
   private static final String ANNOTATION_NAME = "javax.ejb.EJB";
   private static final String ANNOTATION_FIELD_BEAN_INTERFACE = "beanInterface";
   private static final String ANNOTATION_FIELD_MAPPED_NAME = "mappedName";
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestEnricher#enrich(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void enrich(Object testCase)
   {
      if(SecurityActions.isClassPresent(ANNOTATION_NAME)) 
      {
         injectClass(testCase);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestEnricher#resolve(org.jboss.arquillian.spi.Context, java.lang.reflect.Method)
    */
   public Object[] resolve(Method method) 
   {
     return new Object[method.getParameterTypes().length];
   }
   
   /**
    * Obtains all field in the specified class which contain the specified annotation
    * @param clazz
    * @param annotation
    * @return
    * @throws IllegalArgumentException If either argument is not specified
    */
   //TODO Hack, this leaks out privileged operations outside the package.  Extract out properly.
   protected List<Field> getFieldsWithAnnotation(final Class<?> clazz, final Class<? extends Annotation> annotation)
         throws IllegalArgumentException
   {
      // Precondition checks
      if (clazz == null)
      {
         throw new IllegalArgumentException("clazz must be specified");
      }
      if (annotation == null)
      {
         throw new IllegalArgumentException("annotation must be specified");
      }

      // Delegate to the privileged operations
      return SecurityActions.getFieldsWithAnnotation(clazz, annotation);
   }
   
   protected void injectClass(Object testCase) 
   {
      try 
      {
         @SuppressWarnings("unchecked")
         Class<? extends Annotation> ejbAnnotation = (Class<? extends Annotation>)SecurityActions.getThreadContextClassLoader().loadClass(ANNOTATION_NAME);
         
         List<Field> annotatedFields = SecurityActions.getFieldsWithAnnotation(
               testCase.getClass(), 
               ejbAnnotation);
         
         for(Field field : annotatedFields) 
         {
            if(field.get(testCase) == null) // only try to lookup fields that are not already set
            {
               Object ejb = lookupEJB(field.getType());
               field.set(testCase, ejb);
            }
         }
         
         List<Method> methods = SecurityActions.getMethodsWithAnnotation(
               testCase.getClass(), 
               ejbAnnotation);
         
         for(Method method : methods) 
         {
            if(method.getParameterTypes().length != 1) 
            {
               throw new RuntimeException("@EJB only allowed on single argument methods");
            }
            if(!method.getName().startsWith("set")) 
            {
               throw new RuntimeException("@EJB only allowed on 'set' methods");
            }
            Object ejb = lookupEJB(method.getParameterTypes()[0]);
            method.invoke(testCase, ejb);
         }
         
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not inject members", e);
      }
   }
   
   protected Object lookupEJB(Class<?> fieldType) throws Exception 
   {
      // TODO: figure out test context ? 
      InitialContext initcontext = createContext();
      
      String[] jndiNames = {
            "java:global/test.ear/test/" + fieldType.getSimpleName() + "Bean",
            "java:global/test.ear/test/" + fieldType.getSimpleName(),
            "java:global/test/" + fieldType.getSimpleName(),
            "java:global/test/" + fieldType.getSimpleName() + "Bean",
            "java:global/test/" + fieldType.getSimpleName() + "/no-interface",
            "test/" + fieldType.getSimpleName() + "Bean/local",
            "test/" + fieldType.getSimpleName() + "Bean/remote",
            "test/" + fieldType.getSimpleName() + "/no-interface",
            fieldType.getSimpleName() + "Bean/local",
            fieldType.getSimpleName() + "Bean/remote",
            fieldType.getSimpleName() + "/no-interface"
      };
      
      for(String jndiName : jndiNames)
      {
         try 
         {
            return initcontext.lookup(jndiName);
         } 
         catch (NamingException e) 
         {
            // no-op, try next
         }
      }
      throw new NamingException("No EJB found in JNDI, tried the following names: " + joinJndiNames(jndiNames));
   }
   
   protected InitialContext createContext() throws Exception
   {
      return new InitialContext();
   }
   
   // Simple helper for printing the jndi names
   private String joinJndiNames(String[] strings)
   {
      StringBuilder sb = new StringBuilder();
      
      for(String string: strings)
      {
         sb.append(string).append(", ");
      }
      return sb.toString();
   }
}
