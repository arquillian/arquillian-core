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
package org.jboss.arquillian.spi.event.suite;

/**
 * Validate
 * 
 * Validation utility
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
final class Validate
{
   private Validate()
   {
   }

   /**
    * Checks that object is not null, throws exception if it is.
    * 
    * @param obj The object to check
    * @param message The exception message
    * @throws IllegalArgumentException Thrown if obj is null 
    */
   public static void notNull(final Object obj, final String message) throws IllegalArgumentException
   {
      if (obj == null)
      {
         throw new IllegalArgumentException(message);
      }
   }

   /**
    * Checks that the specified String is not null or empty, 
    * throws exception if it is.
    * 
    * @param string The object to check
    * @param message The exception message
    * @throws IllegalArgumentException Thrown if obj is null 
    */
   public static void notNullOrEmpty(final String string, final String message) throws IllegalArgumentException
   {
      if (string == null || string.length() == 0)
      {
         throw new IllegalArgumentException(message);
      }
   }
   
   /**
    * Checks that obj is not null, throws exception if it is.
    * 
    * @param obj The object to check
    * @param message The exception message
    * @throws IllegalStateException Thrown if obj is null
    */
   public static void stateNotNull(final Object obj, final String message) throws IllegalStateException
   {
      if(obj == null)
      {
         throw new IllegalStateException(message);
      }
   }
}
