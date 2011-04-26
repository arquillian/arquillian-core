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
package org.jboss.arquillian.core.spi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.core.spi.context.Context;

/**
 * ManagerBuilder
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ManagerBuilder
{
   private static final String MANAGER_IMPL_CLASS = "org.jboss.arquillian.core.impl.ManagerImpl";
   
   private Set<Class<? extends Context>> contexts;
   private Set<Class<?>> extensions;
   
   public static ManagerBuilder from()
   {
      return new ManagerBuilder();
   }
   
   private ManagerBuilder()
   {
      contexts = new HashSet<Class<? extends Context>>();
      extensions = new HashSet<Class<?>>();
   }

   public ManagerBuilder context(Class<? extends Context> context)
   {
      Validate.notNull(context, "Context must be specified");
      
      contexts.add(context);
      return this;
   }

   public ManagerBuilder extensions(Class<?>... extensions)
   {
      Validate.notNull(extensions, "Extensions must be specified");
      for(Class<?> extension : extensions)
      {
         extension(extension);
      }
      return this;
   }

   public ManagerBuilder extension(Class<?> extension)
   {
      Validate.notNull(extension, "Extension must be specified");
      
      extensions.add(extension);
      return this;
   }
   
   public Manager create()
   {
      return SecurityActions.newInstance(
            MANAGER_IMPL_CLASS, 
            new Class<?>[] {Collection.class, Collection.class},
            new Object[] {contexts, extensions},
            Manager.class);
   }
}
