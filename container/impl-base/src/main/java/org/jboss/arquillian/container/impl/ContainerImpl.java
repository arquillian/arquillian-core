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
package org.jboss.arquillian.container.impl;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.ProtocolDef;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ServerKillProcessor;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.event.container.AfterKill;
import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.container.spi.event.container.BeforeKill;
import org.jboss.arquillian.container.spi.event.container.BeforeSetup;
import org.jboss.arquillian.container.spi.event.container.BeforeStart;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.container.spi.event.container.ContainerEvent;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.Validate;

/**
 * Container
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerImpl implements Container
{
   @Inject
   private Event<ContainerEvent> event;
   
   @Inject @ContainerScoped
   private InstanceProducer<Container> containerProducer;
   
   @Inject
   private Instance<ServiceLoader> serviceLoader;
   
   private DeployableContainer<?> deployableContainer;
   private String name;
   private State state = State.STOPPED;
   private Throwable failureCause;
   
   private ContainerDef containerConfiguration;
   
   public ContainerImpl(String name, DeployableContainer<?> deployableContainer, ContainerDef containerConfiguration)
   {
      Validate.notNull(name, "Name must be specified");
      Validate.notNull(deployableContainer, "DeployableContainer must be specified");
      Validate.notNull(containerConfiguration, "ConfigurationConfiguration must be specified");
      
      this.name = name;
      this.deployableContainer = deployableContainer;
      this.containerConfiguration = containerConfiguration;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.impl.ContainerT#getName()
    */
   @Override
   public String getName()
   {
      return name;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.impl.ContainerT#getDeployableContainer()
    */
   @Override
   public DeployableContainer<?> getDeployableContainer()
   {
      return deployableContainer;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.impl.ContainerT#getContainerConfiguration()
    */
   @Override
   public ContainerDef getContainerConfiguration()
   {
      return containerConfiguration;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.impl.ContainerT#createDeployableConfiguration()
    */
   @Override
   public ContainerConfiguration createDeployableConfiguration() throws Exception
   {
      ContainerConfiguration config = deployableContainer.getConfigurationClass().newInstance();
      MapObject.populate(config, containerConfiguration.getContainerProperties());
      config.validate();
      return config;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.impl.ContainerT#hasProtocolConfiguration(org.jboss.arquillian.spi.client.protocol.ProtocolDescription)
    */
   @Override
   public boolean hasProtocolConfiguration(ProtocolDescription description)
   {
      for(ProtocolDef protocol : containerConfiguration.getProtocols())
      {
         if(description.getName().equals(protocol.getType()))
         {
            return true;
         }
      }
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.impl.ContainerT#getProtocolConfiguration(org.jboss.arquillian.spi.client.protocol.ProtocolDescription)
    */
   @Override
   public ProtocolDef getProtocolConfiguration(ProtocolDescription description)
   {
      for(ProtocolDef protocol : containerConfiguration.getProtocols())
      {
         if(description.getName().equals(protocol.getType()))
         {
            return protocol;
         }
      }
      return null;
   }

   @Override
   public State getState() 
   {
      return state;
   }

   @Override
   public void setState(State state) 
   {
      this.state = state;      
   }

   @Override
   public Throwable getFailureCause() 
   {
      return failureCause;
   }
   
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   public void setup() throws Exception
   {
      event.fire(new BeforeSetup(deployableContainer));
      try 
      {
         /*
          * TODO: should the Container producer some how be automatically registered ?
          * Or should we just 'know' who is the first one to create the context
          */
         containerProducer.set(this);  
         ((DeployableContainer) deployableContainer).setup(createDeployableConfiguration());
         setState(Container.State.SETUP);
      }
      catch (Exception e)
      {
         setState(State.SETUP_FAILED);
         failureCause = e;
         throw e;
      }
      event.fire(new AfterSetup(deployableContainer));
   }

   @Override
   public void start() throws LifecycleException 
   {
      event.fire(new BeforeStart(deployableContainer));
      try 
      {
         deployableContainer.start();
         setState(Container.State.STARTED);
      }
      catch (LifecycleException e)
      {
         setState(State.STARTED_FAILED);
         failureCause = e;
         throw e;
      }
      event.fire(new AfterStart(deployableContainer));
   }

   @Override
   public void stop() throws LifecycleException
   {
      event.fire(new BeforeStop(deployableContainer));
      try 
      {
         deployableContainer.stop();
         setState(Container.State.STOPPED);
      }
      catch (LifecycleException e)
      {
         setState(State.STOPPED_FAILED);
         failureCause = e;
         throw e;
      }
      event.fire(new AfterStop(deployableContainer));
   }
   
   @Override
   public void kill() throws Exception
   {
      event.fire(new BeforeKill(deployableContainer));
      try 
      {
         getServerKillProcessor().kill(this);
         setState(Container.State.KILLED);
      }
      catch (Exception e)
      {
         setState(State.KILLED_FAILED);
         failureCause = e;
         throw e;
      }
      event.fire(new AfterKill(deployableContainer));
   }
   
   private ServerKillProcessor getServerKillProcessor()
   {
      ServiceLoader loader = serviceLoader.get();
      if(loader == null)
      {
         throw new IllegalStateException("No " + ServiceLoader.class.getName() + " found in context");
      }
      
      ServerKillProcessor serverKillProcessor = serviceLoader.get().onlyOne(ServerKillProcessor.class, 
                                                                            DefaultServerKillProcessor.class);
      if(serverKillProcessor == null)
      {
         throw new IllegalStateException("No " + ServerKillProcessor.class.getName() + " found in context");
      }
      return serverKillProcessor;
   }
}
