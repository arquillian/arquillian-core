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

import org.jboss.arquillian.spi.Context;
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
   public void enrich(Context context, Object testCase)
   {
      if(SecurityActions.isClassPresent(ANNOTATION_NAME)) 
      {
         injectClass(context, testCase);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestEnricher#resolve(org.jboss.arquillian.spi.Context, java.lang.reflect.Method)
    */
   public Object[] resolve(Context context, Method method) 
   {
     return new Object[method.getParameterTypes().length];
   }
   
   protected void injectClass(Context context, Object testCase) 
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
            Object ejb = lookupEJB(context, field.getType());
            field.set(testCase, ejb);
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
            Object ejb = lookupEJB(context, method.getParameterTypes()[0]);
            method.invoke(testCase, ejb);
         }
         
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not inject members", e);
      }
   }
   
   protected Object lookupEJB(Context context, Class<?> fieldType) throws Exception 
   {
      // TODO: figure out test context ? 
      InitialContext initcontext = createContext(context);
      try 
      {
         return initcontext.lookup("java:global/test.ear/test/" + fieldType.getSimpleName() + "Bean");
      } 
      catch (NamingException e) 
      {
    	  try 
    	  {
    	     return initcontext.lookup("test/" + fieldType.getSimpleName() + "Bean/local");
    	  } 
    	  catch (NamingException e2) 
    	  {
    	     return initcontext.lookup("test/" + fieldType.getSimpleName() + "Bean/remote");    	    
    	  }
      }
   }
   
   protected InitialContext createContext(Context context) throws Exception
   {
      return new InitialContext();
   }
}
