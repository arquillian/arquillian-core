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
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.core.spi.Validate;

/**
 * Container
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerImpl implements Container
{
   private DeployableContainer<?> deployableContainer;
   private String name;
   
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
}
