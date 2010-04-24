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
package org.jboss.arquillian.spi;

import org.jboss.arquillian.spi.event.Event;
import org.jboss.arquillian.spi.event.suite.EventHandler;


/**
 * 
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * @param <X>
 * @param <T>
 */
public interface Context 
{
   /**
    * Fire a new {@link Event}.
    * 
    * @param event The {@link Event} instance to fire
    */
   void  fire(Event event); 
   
   /**
    * Register a {@link EventHandler} for a specific {@link Event}.
    * 
    * @param <K>
    * @param eventType The Type of {@link Event} to listen to
    * @param handler The receiver of the {@link Event}
    */
   <K extends Event> void register(Class<? extends K> eventType, EventHandler<? super K> handler);

   /**
    * Get the defined {@link ServiceLoader}
    * 
    * @return A instance of {@link ServiceLoader}
    * @see #get(Class)
    */
   ServiceLoader getServiceLoader();
   
   /**
    * Get this contexts parent context.
    * 
    * @return The parent context if any, null if this is the top context.
    */
   Context getParentContext();

   /**
    * Add a instance of B to the context.
    * 
    * @param <B>
    * @param type The Type of the instance to add
    * @param instance The instance to add
    */
   <B> void add(Class<B> type, B instance);
   
   /**
    * Get a instance of B from the context.
    * 
    * @param <B>
    * @param type Type to lookup
    * @return A instance of B or null if not found
    */
   <B> B get(Class<B> type);
}
