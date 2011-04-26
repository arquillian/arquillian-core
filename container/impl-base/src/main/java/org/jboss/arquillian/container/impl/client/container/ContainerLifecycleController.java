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
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StartManagedContainers;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.StopManagedContainers;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.event.container.AfterSetup;
import org.jboss.arquillian.spi.event.container.AfterStart;
import org.jboss.arquillian.spi.event.container.AfterStop;
import org.jboss.arquillian.spi.event.container.BeforeSetup;
import org.jboss.arquillian.spi.event.container.BeforeStart;
import org.jboss.arquillian.spi.event.container.BeforeStop;
import org.jboss.arquillian.spi.event.container.ContainerEvent;

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

   public void startContainers(@Observes StartManagedContainers event) throws Exception
   {
      forEachContainer(new Operation<Container>()
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

   public void stopContainers(@Observes StopManagedContainers event) throws Exception
   {
      forEachContainer(new Operation<Container>()
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
         @Inject
         private Event<ContainerEvent> event;

         @Inject @ContainerScoped
         private InstanceProducer<Container> containerProducer;

         @SuppressWarnings({"rawtypes", "unchecked"})
         @Override
         public void perform(Container container) throws Exception
         {
            /*
             * TODO: should the Container producer some how be automatically registered ?
             * Or should we just 'know' who is the first one to create the context
             */
            containerProducer.set(container);  
            DeployableContainer deployable = container.getDeployableContainer();
            injector.get().inject(deployable);
            
            event.fire(new BeforeSetup(deployable));
            deployable.setup(container.createDeployableConfiguration());
            event.fire(new AfterSetup(deployable));
         }
      });
   }

   public void startContainer(@Observes StartContainer event) throws Exception
   {
      forContainer(event.getContainer(), new Operation<Container>()
      {
         @Inject
         private Event<ContainerEvent> event;
         
         @Override
         public void perform(Container container) throws Exception
         {
            DeployableContainer<?> deployable = container.getDeployableContainer();
            
            event.fire(new BeforeStart(deployable));
            deployable.start();
            event.fire(new AfterStart(deployable));
         }
      });
   }
   
   public void stopContainer(@Observes StopContainer event) throws Exception
   {
      forContainer(event.getContainer(), new Operation<Container>()
      {
         @Inject
         private Event<ContainerEvent> event;

         @Override
         public void perform(Container container) throws Exception
         {
            DeployableContainer<?> deployable = container.getDeployableContainer();
            
            event.fire(new BeforeStop(deployable));
            deployable.stop();
            event.fire(new AfterStop(deployable));
         }
      });
   }
   
   private void forEachContainer(Operation<Container> operation) throws Exception
   {
      injector.get().inject(operation);
      ContainerRegistry registry = containerRegistry.get();
      for(Container container : registry.getContainers())
      {
         operation.perform(container);
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
