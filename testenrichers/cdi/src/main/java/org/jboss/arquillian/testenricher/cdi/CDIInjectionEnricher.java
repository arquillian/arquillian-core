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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;

import org.jboss.arquillian.spi.TestEnricher;

/**
 * CDIInjectionEnricher
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CDIInjectionEnricher implements TestEnricher 
{
   private static final String JNDI_BEAN_MANAGER = "java:comp/BeanManager";
   private static final String JNDI_BEAN_MANAGER_JBOSS = "java:app/BeanManager";
   private static final String ANNOTATION_NAME = "javax.inject.Inject";
   
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
         BeanManager beanManager = lookupBeanManager();
         if(beanManager != null) {
            injectNonContextualInstance(beanManager, testCase);            
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

   protected BeanManager lookupBeanManager() 
   {
      try 
      {
         return (BeanManager)new InitialContext().lookup(JNDI_BEAN_MANAGER);   
      }
      catch (Exception e) 
      {
         // TODO: hack until JBoss fix BeanManager binding 
         try 
         {
            return (BeanManager)new InitialContext().lookup(JNDI_BEAN_MANAGER_JBOSS);
         } 
         catch (Exception e2) 
         {
            return null;
         }
      }
   }
}
