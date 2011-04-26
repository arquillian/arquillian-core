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
package org.jboss.arquillian.junit;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * A set of privileged actions that are not to leak out
 * of this package 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
final class SecurityActions
{

   //-------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   //-------------------------------------------------------------------------------||

   /**
    * No instantiation
    */
   private SecurityActions()
   {
      throw new UnsupportedOperationException("No instantiation");
   }

   //-------------------------------------------------------------------------------||
   // Utility Methods --------------------------------------------------------------||
   //-------------------------------------------------------------------------------||

   /**
    * Obtains the Thread Context ClassLoader
    */
   static ClassLoader getThreadContextClassLoader()
   {
      return AccessController.doPrivileged(GetTcclAction.INSTANCE);
   }
   
   static boolean existsClass(String className)
   {
      try
      {
         loadClass(className);
         return true;
      }
      catch (Exception e) 
      {
         return false;
      }
   }

   static Class<?> loadClass(String className)
   {
      try
      {
         return Class.forName(className, true, getThreadContextClassLoader());
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not load class", e);
      }
   }


   //-------------------------------------------------------------------------------||
   // Inner Classes ----------------------------------------------------------------||
   //-------------------------------------------------------------------------------||

   /**
    * Single instance to get the TCCL
    */
   private enum GetTcclAction implements PrivilegedAction<ClassLoader> {
      INSTANCE;

      @Override
      public ClassLoader run()
      {
         return Thread.currentThread().getContextClassLoader();
      }

   }

}
