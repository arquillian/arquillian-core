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
package org.jboss.arquillian.spi.util;

import java.lang.reflect.Method;

import org.jboss.arquillian.spi.TestEnricher;

/**
 * TestEnrichers
 * 
 * Helper for enriching TestCase instances based on multiple TestEnrichers.
 * 
 * @deprecated When TestNG get support for Phases Listeners, this should be moved out as a EventHandler in the before phase. Remove ServiceLoader
 * 
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Deprecated
public class TestEnrichers
{
   private TestEnrichers()
   {
   }

   /**
    * Enrich the method arguments of a method call.<br/>
    * The Object[] index will match the method parameterType[] index.
    * 
    * @param method
    * @return the argument values
    */
   public static Object[] enrich(Method method)
   {
      Object[] values = new Object[method.getParameterTypes().length];
      ServiceLoader<TestEnricher> serviceLoader = ServiceLoader
            .load(TestEnricher.class);
      for (TestEnricher enricher : serviceLoader)
      {
         mergeValues(values, enricher.resolve(method));
      }
      return values;
   }

   private static void mergeValues(Object[] values, Object[] resolvedValues)
   {
      if(resolvedValues == null || resolvedValues.length == 0)
      {
         return;
      }
      if(values.length != resolvedValues.length)
      {
         throw new IllegalStateException("TestEnricher resolved wrong argument count, expected " + 
               values.length + " returned " + resolvedValues.length);
      }
      for (int i = 0; i < resolvedValues.length; i++)
      {
         Object resvoledValue = resolvedValues[i];
         if (resvoledValue != null && values[i] == null)
         {
            values[i] = resvoledValue;
         }
      }
   }

}
