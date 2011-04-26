/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.core.impl;

public final class UncheckedThrow {
   private UncheckedThrow(){}

   public static void throwUnchecked(final Throwable ex){
       // Now we use the 'generic' method. Normally the type T is inferred
       // from the parameters. However you can specify the type also explicit!
       // Now we do just that! We use the RuntimeException as type!
       // That means the throwsUnchecked throws an unchecked exception!
       // Since the types are erased, no type-information is there to prevent this!
       UncheckedThrow.<RuntimeException>throwsUnchecked(ex);
   }

   /**
    * Generics are erased in Java. The real Type of T is lost during the compilation
    */
   @SuppressWarnings("unchecked")
   private static <T extends Throwable> void throwsUnchecked(Throwable toThrow) throws T{
       // Since the type is erased, this cast actually does nothing!!!
       // we can throw any exception
       throw (T) toThrow;
   }
}