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

import java.util.List;

import junit.framework.Assert;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.junit.Test;

/**
 * TestCase to ensure a nested observes Exception is only handled once within the call chain.
 * 
 * - TestEventFire
 *   - TestExceptionThrower
 *     - throws Exception
 *       - TestExceptionHandler
 *         - re throw
 *         
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ExceptionHandlingTestCase extends AbstractManagerTestBase
{
   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(TestEventFire.class);
      extensions.add(TestExceptionThrower.class);
      extensions.add(TestExceptionHandler.class);
   }
   
   @Test
   public void shouldOnlyFireSameExceptionOnce() throws Exception
   {
      try
      {
         fire(new Double(0.0));
      }
      catch (Exception e) 
      {
         if(!(e instanceof TestException))
         {
            Assert.fail("Wrong Exception thrown " + e);
         }
      }
      
      Assert.assertEquals(new Integer(1), TestEventFire.called);
      Assert.assertEquals(new Integer(1), TestExceptionThrower.called);
      Assert.assertEquals(new Integer(1), TestExceptionHandler.called);
   }
   
   public static class TestEventFire 
   {
      public static Integer called = 0;
      
      @Inject
      private Event<Integer> integer;
      
      public void handle(@Observes Double event) throws Exception
      {
         called++;
         integer.fire(new Integer(10));
      }
   }
   
   public static class TestExceptionThrower
   {
      public static Integer called = 0;
      
      public void handle(@Observes Integer event) throws Exception
      {
         called++;
         throw new TestException("_TEST_");
      }
   }
   
   public static class TestExceptionHandler
   {
      public static Integer called = 0;
      
      public void handle(@Observes TestException event) throws Exception
      {
         called++;
         // Handles exception but re-throws it to specify it's a IllegalException
         throw event;
      }
   }
   
   public static class TestException extends Exception
   {
      private static final long serialVersionUID = 1L;

      public TestException(String message)
      {
         super(message);
      }
   }
}
