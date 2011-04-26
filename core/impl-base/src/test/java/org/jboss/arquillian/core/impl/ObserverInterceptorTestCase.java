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

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * ObserverInterceptorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ObserverInterceptorTestCase extends AbstractManagerTestBase
{
   public static List<String> callStack = new ArrayList<String>();
   
   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(InterceptorObserver.class);
      extensions.add(InterceptorObserver2.class); 
      extensions.add(Observer.class);
   }

   @Test
   public void shouldInterceptEvent() throws Exception
   {
      fire("test");
      
      Assert.assertEquals(InterceptorObserver2.class.getSimpleName(), callStack.get(0));
      Assert.assertEquals(InterceptorObserver.class.getSimpleName(), callStack.get(1));
      Assert.assertEquals(Observer.class.getSimpleName(), callStack.get(2));
      Assert.assertEquals(InterceptorObserver.class.getSimpleName(), callStack.get(3));
      Assert.assertEquals(InterceptorObserver2.class.getSimpleName(), callStack.get(4));
   }
   
   public static class Observer 
   {
      public void around(@Observes String event)
      {
         callStack.add(Observer.class.getSimpleName());
      }
   }

   public static class InterceptorObserver 
   {
      public void around(@Observes EventContext<String> event)
      {
         callStack.add(InterceptorObserver.class.getSimpleName());
         try
         {
            event.proceed();
         }
         finally
         {
            callStack.add(InterceptorObserver.class.getSimpleName());
         }
      }
   }
   
   public static class InterceptorObserver2 
   {
      public void around(@Observes(precedence = 1) EventContext<String> event)
      {
         callStack.add(InterceptorObserver2.class.getSimpleName());
         try
         {
            event.proceed();
         }
         finally
         {
            callStack.add(InterceptorObserver2.class.getSimpleName());
         }
      }
   }

}
