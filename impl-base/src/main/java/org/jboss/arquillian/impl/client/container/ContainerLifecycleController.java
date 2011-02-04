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
package org.jboss.arquillian.impl.client.container;

import org.jboss.arquillian.impl.ThreadContext;
import org.jboss.arquillian.impl.client.container.event.SetupContainer;
import org.jboss.arquillian.impl.client.container.event.SetupContainers;
import org.jboss.arquillian.impl.client.container.event.StartContainer;
import org.jboss.arquillian.impl.client.container.event.StartManagedContainers;
import org.jboss.arquillian.impl.client.container.event.StopContainer;
import org.jboss.arquillian.impl.client.container.event.StopManagedContainers;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ContainerScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
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
   private Instance<ContainerContext> containerContext;

   @Inject 
   private Instance<ContainerRegistry> containerRegistry;

   @Inject
   private Instance<Injector> injector;
   
   public void setupContainers(@Observes SetupContainers event) throws Exception
   {
      forEachContainer(new Operation<String>()
      {
         @Inject
         private Event<SetupContainer> event;

         @Override
         public void perform(String container)
         {
            event.fire(new SetupContainer(container));
         }
      });
   }

   public void startContainers(@Observes StartManagedContainers event) throws Exception
   {
      forEachContainer(new Operation<String>()
      {
         @Inject
         private Event<StartContainer> event;

         @Override
         public void perform(String container)
         {
            event.fire(new StartContainer(container));
         }
      });
   }

   public void stopContainers(@Observes StopManagedContainers event) throws Exception
   {
      forEachContainer(new Operation<String>()
      {
         @Inject
         private Event<StopContainer> stopContainer;

         @Override
         public void perform(String container)
         {
            stopContainer.fire(new StopContainer(container));
         }
      });
   }
   
   public void setupContainer(@Observes SetupContainer event) throws Exception
   {
      forContainer(event.getContainerName(), new Operation<Container>()
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
      forContainer(event.getContainerName(), new Operation<Container>()
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
   
   public void startContainers(@Observes StopContainer event) throws Exception
   {
      forContainer(event.getContainerName(), new Operation<Container>()
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

   private void forEachContainer(Operation<String> operation) throws Exception
   {
      injector.get().inject(operation);
      ContainerRegistry registry = containerRegistry.get();
      for(Container container : registry.getContainers())
      {
         ThreadContext.set(container.getClassLoader());
         containerContext.get().activate(container.getName());
         try
         {
            operation.perform(container.getName());
         }
         finally
         {
            containerContext.get().deactivate();
            ThreadContext.reset();
         }
      }
   }

   private void forContainer(String containerName, Operation<Container> operation) throws Exception
   {
      injector.get().inject(operation);
      ContainerRegistry registry = containerRegistry.get();
      Container container = registry.getContainer(containerName);
      operation.perform(container);
   }

   public interface Operation<T>
   {
      void perform(T container) throws Exception;
   }
}
