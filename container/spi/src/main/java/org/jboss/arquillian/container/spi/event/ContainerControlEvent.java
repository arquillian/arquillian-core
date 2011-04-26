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
package org.jboss.arquillian.container.spi.event;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.core.spi.event.Event;

/**
 * ContainerControlEvent
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class ContainerControlEvent implements Event
{
   private Container container;

   public ContainerControlEvent(Container container)
   {
      this.container = container;
   }

   /**
    * @return the container
    */
   public Container getContainer()
   {
      return container;
   }
   
   /**
    * @return
    */
   public String getContainerName()
   {
      return getContainer().getName();
   }
}
