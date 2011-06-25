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
package org.jboss.arquillian.test.spi;

import java.lang.reflect.Constructor;

import org.junit.Test;

/**
 * ExceptionProxyTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ExceptionProxyTestCase
{
   public static String MSG = "_TEST_";
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldProxyIllegalArgumentException() throws Throwable 
   {
      proxy(new IllegalArgumentException(MSG));
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void shouldProxyExtendedIllegalArgumentException() throws Throwable 
   {
      proxy(new ExtendedIllegalArgumentException(new Exception(MSG)));
   }

   @Test(expected = UnsatisfiedResolutionException.class)
   public void shouldProxyUnsatisfiedResolutionException() throws Throwable 
   {
      proxy(new UnsatisfiedResolutionException(new Exception(MSG)));
   }

   private void proxy(Throwable throwable) throws Throwable
   {

      //printConstructors(throwable);
      
      throw ExceptionProxy.createForException(throwable).createException();
   }

   /**
    * @param throwable
    */
   private void printConstructors(Throwable throwable) throws Exception
   {
      System.out.println("Declared-Constrcutors for: " + throwable.getClass());
      for(Constructor<?> constructor : throwable.getClass().getDeclaredConstructors())
      {
         System.out.println(constructor);
      }
   }

   // Simulate org.jboss.weld.exceptions.IllegalArgumentException
   private static class ExtendedIllegalArgumentException extends IllegalArgumentException
   {
      private static final long serialVersionUID = 1L;

      public ExtendedIllegalArgumentException(Exception throwable)
      {
         super(throwable);
      }
   }
   
   // simulate javax.enterprise.inject
   public static class UnsatisfiedResolutionException extends ResolutionException
   {
      private static final long serialVersionUID = 5350603312442756709L;

      public UnsatisfiedResolutionException()
      {
         super();
      }

      public UnsatisfiedResolutionException(String message, Throwable throwable)
      {
         super(message, throwable);
      }

      public UnsatisfiedResolutionException(String message)
      {
         super(message);
      }

      public UnsatisfiedResolutionException(Throwable throwable)
      {
         super(throwable);
      }
   }

   public static class ResolutionException extends InjectionException 
   {
      private static final long serialVersionUID = -6280627846071966243L;

      public ResolutionException()
      {
         super();
      }

      public ResolutionException(String message, Throwable cause)
      {
         super(message, cause);
      }

      public ResolutionException(String message)
      {
         super(message);
      }

      public ResolutionException(Throwable cause)
      {
         super(cause);
      }
   }

   public static class InjectionException extends RuntimeException
   {
      private static final long serialVersionUID = -2132733164534544788L;

      public InjectionException()
      {
      }
      
      public InjectionException(String message, Throwable throwable)
      {
         super(message, throwable);
      }
      
      public InjectionException(String message)
      {
         super(message);
      }
      
      public InjectionException(Throwable throwable)
      {
         super(throwable);
      }
   }

}
