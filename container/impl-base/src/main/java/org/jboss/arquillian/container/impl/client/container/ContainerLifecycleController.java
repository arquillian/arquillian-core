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
package org.jboss.arquillian.container.impl.client.container;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.event.KillContainer;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StartSuiteContainers;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.StopSuiteContainers;
import org.jboss.arquillian.container.spi.event.StopManualContainers;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * ContainerController
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerLifecycleController
{
   @Inject 
   private Instance<ContainerRegistry> containerRegistry;

   @Inject
   private Instance<Injector> injector;
   
   public void setupContainers(@Observes SetupContainers event) throws Exception
   {
      forEachContainer(new Operation<Container>()
      {
         @Inject
         private Event<SetupContainer> event;

         @Override
         public void perform(Container container)
         {
            event.fire(new SetupContainer(container));
         }
      });
   }

   public void startSuiteContainers(@Observes StartSuiteContainers event) throws Exception
   {
      forEachSuiteContainer(new Operation<Container>()
      {
         @Inject
         private Event<StartContainer> event;

         @Override
         public void perform(Container container)
         {
            event.fire(new StartContainer(container));
         }
      });
   }

   public void stopSuiteContainers(@Observes StopSuiteContainers event) throws Exception
   {
      forEachSuiteContainer(new Operation<Container>()
      {
         @Inject
         private Event<StopContainer> stopContainer;

         @Override
         public void perform(Container container)
         {
            stopContainer.fire(new StopContainer(container));
         }
      });
   }
   
   public void stopManualContainers(@Observes StopManualContainers event) throws Exception
   {
      forEachManualContainer(new Operation<Container>()
      {
         @Inject
         private Event<StopContainer> stopContainer;
         
         @Override
         public void perform(Container container)
         {
            stopContainer.fire(new StopContainer(container));
         }
      });
   }
   
   public void setupContainer(@Observes SetupContainer event) throws Exception
   {
      forContainer(event.getContainer(), new Operation<Container>()
      {
         @Override
         public void perform(Container container) throws Exception
         {
            container.setup();
         }
      });
   }

   public void startContainer(@Observes StartContainer event) throws Exception
   {
      forContainer(event.getContainer(), new Operation<Container>()
      {
         @Override
         public void perform(Container container) throws Exception
         {
            if (!container.getState().equals(Container.State.STARTED)) 
            {
               container.start();
            }
         }
      });
   }
   
   public void stopContainer(@Observes StopContainer event) throws Exception
   {
      forContainer(event.getContainer(), new Operation<Container>()
      {
         @Override
         public void perform(Container container) throws Exception
         {
            if (container.getState().equals(Container.State.STARTED)) 
            {
               container.stop();
            }
         }
      });
   }
   
   public void killContainer(@Observes KillContainer event) throws Exception
   {
      forContainer(event.getContainer(), new Operation<Container>()
      {
         @Override
         public void perform(Container container) throws Exception
         {
            if (container.getState().equals(Container.State.STARTED)) 
            {
               container.kill();
            }
         }
      });
   }
   
   private void forEachContainer(Operation<Container> operation) throws Exception
   {
      injector.get().inject(operation);
      ContainerRegistry registry = containerRegistry.get();
      if(registry == null)
      {
         return;
      }
      for(Container container : registry.getContainers())
      {
         operation.perform(container);
      }
   }
   
   private void forEachSuiteContainer(Operation<Container> operation) throws Exception
   {
      injector.get().inject(operation);
      ContainerRegistry registry = containerRegistry.get();
      for(Container container : registry.getContainers())
      {
         if ("suite".equals(container.getContainerConfiguration().getMode())) 
         {
            operation.perform(container);
         }
      }
   }
   
   private void forEachManualContainer(Operation<Container> operation) throws Exception
   {
      injector.get().inject(operation);
      ContainerRegistry registry = containerRegistry.get();
      for(Container container : registry.getContainers())
      {
         if ("manual".equals(container.getContainerConfiguration().getMode())) 
         {
            operation.perform(container);
         }
      }
   }
   
   private void forContainer(Container container, Operation<Container> operation) throws Exception
   {
      injector.get().inject(operation);
      operation.perform(container);
   }

   public interface Operation<T>
   {
      void perform(T container) throws Exception;
   }
}
