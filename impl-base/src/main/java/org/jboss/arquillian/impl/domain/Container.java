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
package org.jboss.arquillian.impl.domain;

import org.jboss.arquillian.impl.MapObject;
import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.impl.configuration.model.ContainerImpl;
import org.jboss.arquillian.impl.configuration.model.ProtocolImpl;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;

/**
 * Container
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class Container
{
   private ClassLoader classLoader;
   
   private DeployableContainer<?> deployableContainer;
   private String name;
   
   private ContainerImpl containerConfiguration;
   
   public Container(String name, ClassLoader classLoader, DeployableContainer<?> deployableContainer, ContainerImpl containerConfiguration)
   {
      Validate.notNull(name, "Name must be specified");
      Validate.notNull(classLoader, "ClassLoader must be specified");
      Validate.notNull(deployableContainer, "DeployableContainer must be specified");
      Validate.notNull(containerConfiguration, "ConfigurationConfiguration must be specified");
      
      this.name = name;
      this.classLoader = classLoader;
      this.deployableContainer = deployableContainer;
      this.containerConfiguration = containerConfiguration;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @return the deployableContainer
    */
   public DeployableContainer<?> getDeployableContainer()
   {
      return deployableContainer;
   }
   
   /**
    * @return the containerClassLoader
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    * @return the containerConfiguration
    */
   public ContainerImpl getContainerConfiguration()
   {
      return containerConfiguration;
   }
   
   /**
    * @return the configuration
    */
   public ContainerConfiguration createDeployableConfiguration() throws Exception
   {
      ContainerConfiguration config = deployableContainer.getConfigurationClass().newInstance();
      MapObject.populate(config, containerConfiguration.getProperties());
      return config;
   }
   
   public boolean hasProtocolConfiguration(ProtocolDescription description)
   {
      for(ProtocolImpl protocol : containerConfiguration.getProtocols())
      {
         if(description.getName().equals(protocol.getType()))
         {
            return true;
         }
      }
      return false;
   }

   public ProtocolImpl getProtocolConfiguration(ProtocolDescription description)
   {
      for(ProtocolImpl protocol : containerConfiguration.getProtocols())
      {
         if(description.getName().equals(protocol.getType()))
         {
            return protocol;
         }
      }
      return null;
   }
}
