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
package org.jboss.arquillian.core.impl;

import junit.framework.Assert;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.junit.Test;

/**
 * ObserverMethodAvailabilityFilterTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ObserverMethodAvailabilityFilterTestCase
{
   
   @Test
   public void shouldCallFilteredMethodsIfInContext() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
                     .extension(ObserverMultiArgument.class)
                     .create();
      
      manager.getContext(ApplicationContext.class).getObjectStore().add(Integer.class, 10);
      
      manager.fire(new String("_TEST_"));
      
      ObserverMultiArgument extension = manager.getExtension(ObserverMultiArgument.class);
      
      Assert.assertTrue(
            "Non filtered method should have been called", 
            extension.wasCalled);
      
      Assert.assertTrue(
            "Filtered method should not have been called, filter not in context", 
            extension.filteredWasCalled);
   }

   @Test
   public void shouldNotCallFilteredMethodsIfNotInContext() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
                     .extension(ObserverMultiArgument.class)
                     .create();
      
      manager.fire(new String("_TEST_"));
      
      ObserverMultiArgument extension = manager.getExtension(ObserverMultiArgument.class);
      
      Assert.assertTrue(
            "Non filtered method should have been called", 
            extension.wasCalled);
      
      Assert.assertFalse(
            "Filtered method should not have been called, filter not in context", 
            extension.filteredWasCalled);
   }

   public static class ObserverMultiArgument 
   {
      private boolean wasCalled = false;
      private boolean filteredWasCalled = false;
      
      public void single(@Observes String test)
      {
         wasCalled = true;
      }

      public void filtered(@Observes String test, Integer filter)
      {
         Assert.assertNotNull(filter);
         filteredWasCalled = true;
      }
   }
}
