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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.Event;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * EventManager
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@SuppressWarnings("unchecked")
public class MapEventManager implements EventManager
{
   private Map<Class<? extends Event>, List<EventHandler>> handlerRegistry;
   
   public MapEventManager()
   {
      this.handlerRegistry = new ConcurrentHashMap<Class<? extends Event>, List<EventHandler>>();
   }
   
   public void fire(Context context, Event event) 
   {
      Validate.notNull(context, "Context must be specified");
      Validate.notNull(event, "Event must be specified");

      List<EventHandler> handlers = handlerRegistry.get(event.getClass());
      if(handlers != null)
      {
         try
         {
            /*
               We need to use old style of for loop to avoid ConcurrentModificationException
               A handler could add a listener to the same event in it's callback.
               ie: a DeployableContainer adding a SessionLifecyle handler to be added to the @BeforeClass 
            */ 
            for(int i = 0; i < handlers.size(); i++)
            {
               EventHandler handler = handlers.get(i);
               handler.callback(context, event);
            }
         } 
         catch (Exception e) 
         {
            throw new FiredEventException(context, event, e);
         }
      }
   }
   
   // TODO: look at concurrency of add / list
   public <K extends Event> void register(Class<? extends K> eventType, EventHandler<? super K> handler) 
   {
      Validate.notNull(eventType, "EventType must be specified");
      Validate.notNull(handler, "EventHandler must be specified");
   
      List<EventHandler> handlers = handlerRegistry.get(eventType);
      if(handlers == null)
      {
         handlers = new ArrayList<EventHandler>();
      }
      handlers.add(handler);
      handlerRegistry.put(eventType, handlers);
   }
}
