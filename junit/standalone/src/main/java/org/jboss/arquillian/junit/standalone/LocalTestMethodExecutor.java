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
package org.jboss.arquillian.junit.standalone;

import java.lang.reflect.Method;
import java.util.Collection;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.event.suite.Test;
import org.jboss.arquillian.test.spi.annotation.TestScoped;

/**
 * LocalTestMethodExecutor
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LocalTestMethodExecutor
{
   @Inject
   private Instance<Injector> injector;

   @Inject 
   private Instance<ServiceLoader> serviceLoader;
   
   @Inject @TestScoped
   private InstanceProducer<TestResult> testResult;
   
   public void execute(@Observes Test event) throws Exception 
   {
      TestResult result = new TestResult();
      try 
      {
         event.getTestMethodExecutor().invoke(
               enrichArguments(
                     event.getTestMethod(), 
                     serviceLoader.get().all(TestEnricher.class)));
         result.setStatus(Status.PASSED);
      } 
      catch (Throwable e) 
      {
         result.setStatus(Status.FAILED);
         result.setThrowable(e);
      }
      finally 
      {
         result.setEnd(System.currentTimeMillis());         
      }
      testResult.set(result);
   }

   /**
    * Enrich the method arguments of a method call.<br/>
    * The Object[] index will match the method parameterType[] index.
    * 
    * @param method
    * @return the argument values
    */
   private Object[] enrichArguments(Method method, Collection<TestEnricher> enrichers)
   {
      Object[] values = new Object[method.getParameterTypes().length];
      if(method.getParameterTypes().length == 0)
      {
         return values;
      }
      for (TestEnricher enricher : enrichers)
      {
         injector.get().inject(enricher);
         mergeValues(values, enricher.resolve(method));
      }
      return values;
   }

   private void mergeValues(Object[] values, Object[] resolvedValues)
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
