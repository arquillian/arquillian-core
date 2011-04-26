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

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.api.InstanceProducer;

/**
 * InstanceImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class InstanceImpl<T> implements InstanceProducer<T>
{
   private ManagerImpl manager;
   private Class<T> type;
   private Class<? extends Annotation> scope;
   
   //-------------------------------------------------------------------------------------||
   // Public Factory Methods -------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public static <X> InstanceImpl<X> of(Class<X> type, Class<? extends Annotation> scope, ManagerImpl manager)
   {
      return new InstanceImpl<X>(type, scope, manager);
   }
   
   InstanceImpl(Class<T> type, Class<? extends Annotation> scope, ManagerImpl manager)
   {
      this.type = type;
      this.scope = scope;
      this.manager = manager;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - Instance ------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Instance#get()
    */
   @Override
   public T get()
   {
      return manager.resolve(type);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Instance#set(java.lang.Object)
    */
   @Override
   public void set(T value)
   {
      if(scope == null)
      {
         throw new IllegalStateException("Value can not be set, instance has no Scope defined: " + value);
      }
      manager.bindAndFire(scope, type, value);
   }
}
