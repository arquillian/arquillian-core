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


import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.junit.Assert;
import org.junit.Test;


/**
 * EventFireTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EventFireTestCase
{
   @Test
   public void shouldBeAbleToFireEventToAExtension() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .extension(ExtensionWithObservers.class).create();
      
      manager.fire(new Object());
      
      Assert.assertTrue(manager.getExtension(ExtensionWithObservers.class).methodOneWasCalled);
   }
   
   @Test
   public void shouldBeAbleToFireExceptionEventOnFailingObserver() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .extensions(ExtensionWithExceptionObserver.class, ExtensionObservingException.class).create();
      
      manager.fire(new String("should cause exception"));
      
      Assert.assertTrue(manager.getExtension(ExtensionObservingException.class).methodOneWasCalled);
   }

   @Test(expected = IOException.class)
   public void shouldBeAbleToDetectExceptionEventLoopAndThrowOriginalException() throws Exception
   {
      ManagerImpl manager = (ManagerImpl)ManagerBuilder.from()
         .extensions(ExtensionObservingExceptionLoop.class).create();
      
      manager.fire(new IOException("should cause exception"));
      
  }

   private static class ExtensionWithObservers 
   {
      private boolean methodOneWasCalled = false;

      @SuppressWarnings("unused")
      public void methodOne(@Observes Object object)
      {
         methodOneWasCalled = true;
      }
   }

   private static class ExtensionWithExceptionObserver 
   {
      @SuppressWarnings("unused")
      public void methodOne(@Observes String object)
      {
         throw new IllegalStateException("Illegal state");
      }
   }

   private static class ExtensionObservingException 
   {
      private boolean methodOneWasCalled = false;

      @SuppressWarnings("unused")
      public void methodOne(@Observes IllegalStateException exception)
      {
         methodOneWasCalled = true;
      }
   }

   private static class ExtensionObservingExceptionLoop 
   {
      @SuppressWarnings("unused")
      public void methodOne(@Observes IOException exception) throws IOException
      {
         throw new IOException();
      }
   }
}
