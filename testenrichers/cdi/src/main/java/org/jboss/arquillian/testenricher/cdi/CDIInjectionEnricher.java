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
package org.jboss.arquillian.testenricher.cdi;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestEnricher;

/**
 * Enricher that provide JSR-299 CDI class and method argument injection.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CDIInjectionEnricher implements TestEnricher 
{
   private static final String STANDARD_BEAN_MANAGER_JNDI_NAME = "java:comp/BeanManager";
   private static final String SERVLET_BEAN_MANAGER_JNDI_NAME = "java:comp/env/BeanManager";
   private static final String JBOSSAS_BEAN_MANAGER_JNDI_NAME = "java:global/test/arquillian-protocol/BeanManager";
   private static final String ANNOTATION_NAME = "javax.inject.Inject";

   private static final Logger log = Logger.getLogger(CDIInjectionEnricher.class.getName());
   
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
     Object[] values = new Object[method.getParameterTypes().length];
     if(SecurityActions.isClassPresent(ANNOTATION_NAME)) 
     {
        BeanManager beanManager = lookupBeanManager(context);
        if(beanManager == null) 
        {
             return values;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i < parameterTypes.length; i++)
        {
           values[i] = getInstanceByType(beanManager, i, method);
        }
     }
     return values;
   }
   
   @SuppressWarnings("unchecked")
   private <T> T getInstanceByType(BeanManager manager, final int position, final Method method)
   {
      CreationalContext<?> cc = manager.createCreationalContext(null);
      return (T)manager.getInjectableReference(
            new MethodParameterInjectionPoint<T>(method, position, manager), 
            cc);
   }
   
   protected void injectClass(Context context, Object testCase) 
   {
      try 
      {
         BeanManager beanManager = lookupBeanManager(context);
         if(beanManager != null) {
            injectNonContextualInstance(beanManager, testCase);            
         }
         else
         {
            // Better would be to raise an exception if @Inject is present in class and BeanManager cannot be found
            log.info("BeanManager cannot be located at " + STANDARD_BEAN_MANAGER_JNDI_NAME + ". " +
            		"Either you are using an archive with no beans.xml, or the BeanManager has not been bound to that location in JNDI.");
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not inject members", e);
      }
   }
   
   @SuppressWarnings("unchecked")
   protected void injectNonContextualInstance(BeanManager manager, Object instance)
   {
      CreationalContext<Object> creationalContext =  manager.createCreationalContext(null);
      InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) manager.createInjectionTarget(
            manager.createAnnotatedType(instance.getClass()));
      injectionTarget.inject(instance, creationalContext);
   }

   protected BeanManager lookupBeanManager(Context context) 
   {
      try 
      {
         return (BeanManager) new InitialContext().lookup(STANDARD_BEAN_MANAGER_JNDI_NAME);
      }
      catch (Exception e) 
      {
         try
         {
            return (BeanManager) new InitialContext().lookup(SERVLET_BEAN_MANAGER_JNDI_NAME);
         }
         catch (Exception se) {}

         // TODO: hack until BeanManager binding fixed in JBoss AS
         try 
         {
            return (BeanManager) new InitialContext().lookup(JBOSSAS_BEAN_MANAGER_JNDI_NAME);
         } 
         catch (Exception je)
         {
            return null;
         }
      }
   }
}
