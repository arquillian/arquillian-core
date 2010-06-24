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
package org.jboss.arquillian.testenricher.osgi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestEnricher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The OSGi TestEnricher
 * 
 * The enricher supports the injection of the Framework and the Bundle under test.
 * 
 * <pre><code>
 *    @Inject
 *    BundleContext sysctx;
 * 
 *    @Inject
 *    Bundle bundle;
 * </code></pre>
 * 
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiTestEnricher implements TestEnricher
{
   @Override
   public void enrich(Context context, Object testCase)
   {
      Class<? extends Object> testClass = testCase.getClass();
      enrichInternal(context, testClass, testCase);
   }

   /**
    * Enrich the static fields on the test case
    */
   public void enrich(Context context, Class<?> testClass)
   {
      enrichInternal(context, testClass, null);
   }

   private void enrichInternal(Context context, Class<?> testClass, Object testCase)
   {
      for (Field field : testClass.getDeclaredFields())
      {
         if (field.isAnnotationPresent(Inject.class))
         {
            if (field.getType().isAssignableFrom(BundleContext.class))
            {
               injectBundleContext(context, testCase, field);
            }
            if (field.getType().isAssignableFrom(Bundle.class))
            {
               injectBundle(context, testCase, field);
            }
         }
      }
   }

   private void injectBundleContext(Context context, Object testCase, Field field) 
   {
      try
      {
         BundleContext sysctx = context.get(BundleContext.class);
         field.set(testCase, sysctx);
      }
      catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Cannot inject BundleContext", ex);
      }
   }

   private void injectBundle(Context context, Object testCase, Field field) 
   {
      try
      {
         Bundle bundle = context.get(Bundle.class);
         field.set(testCase, bundle);
      }
      catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Cannot inject Bundle", ex);
      }
   }

   @Override
   public Object[] resolve(Context context, Method method)
   {
      return null;
   }
}
