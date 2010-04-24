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
package org.jboss.arquillian.impl.event;

import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * EventManagerTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class EventManagerTestCase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock 
   private EventHandler<SuiteEvent> handler;

   @Test(expected = FiredEventException.class)
   public void shouldWrapException() throws Exception 
   {
      Mockito.doThrow(new Exception())
            .when(handler).callback(
                  Mockito.any(SuiteContext.class), Mockito.any(SuiteEvent.class));
      
      SuiteContext context = new SuiteContext(serviceLoader);
      SuiteEvent event =  new SuiteEvent();
      
      EventManager manager = new MapEventManager();
      manager.register(SuiteEvent.class, handler);
      
      manager.fire(context, event);
   }

   @Test
   public void shouldBeAbleToRegisterAndFireToMultipleHandlersOnSameEvent() throws Exception 
   {
      int handlerCount = 2;
      
      SuiteContext context = new SuiteContext(serviceLoader);
      SuiteEvent event =  new SuiteEvent();
      
      EventManager manager = new MapEventManager();
      for(int i = 0; i < handlerCount; i++)
      {
         manager.register(SuiteEvent.class, handler);
      }
      manager.fire(context, event);
      
      Mockito.verify(handler, Mockito.times(handlerCount)).callback(context, event);
   }

}
