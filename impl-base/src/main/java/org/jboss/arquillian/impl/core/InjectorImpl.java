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
package org.jboss.arquillian.impl.core;

import org.jboss.arquillian.impl.core.spi.Manager;
import org.jboss.arquillian.spi.core.Injector;

/**
 * InjectorImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class InjectorImpl implements Injector
{
   private Manager manager;
   
   //-------------------------------------------------------------------------------------||
   // Public Factory Methods -------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public static InjectorImpl of(Manager manager)
   {
      return new InjectorImpl(manager);
   }
   
   InjectorImpl(Manager manager)
   {
      this.manager = manager;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - Injector ------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Injector#inject(java.lang.Object)
    */
   @Override
   public void inject(Object target)
   {
      Validate.notNull(target, "Target must be specified.");
      manager.inject(target);
   }
}
