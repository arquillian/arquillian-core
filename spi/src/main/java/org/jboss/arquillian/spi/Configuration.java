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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Holds the global Arquillian configuration and a Map of {@link ContainerConfiguration}
 * implementations objects. It is built by 
 * {@link org.jboss.arquillian.impl.ConfigurationBuilder}s
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @version $Revision: $
 */
public class Configuration
{  
   /**
    * A Map of container configuration objects
    */
   private Map<Class<? extends ContainerConfiguration>, ContainerConfiguration> containersConfig = 
      new HashMap<Class<? extends ContainerConfiguration>, ContainerConfiguration>();

   private String deploymentExportPath = null;
   
   /**
    * Puts a {@link ContainerConfiguration} implementation in the containersConfig
    * field. If the {@link ContainerConfiguration} already exists, it just replaces
    * it.
    * @param containerConfig the {@link ContainerConfiguration} implementation to put.
    */
   public  void addContainerConfig(ContainerConfiguration containerConfig) 
   {
      containersConfig.put(containerConfig.getClass(), containerConfig);
   }

   /**
    * Retrieves a {@link ContainerConfiguration} implementation that matches the clazz
    * parameter.
    * @param <T>
    * @param clazz The actual class of the {@link ContainerConfiguration} we are looking
    * for.
    * @return the {@link ContainerConfiguration} implementation that matches the clazz
    * parameter, null otherwise.
    */
   public <T extends ContainerConfiguration> T getContainerConfig(Class<T> clazz)
   {
      return clazz.cast(containersConfig.get(clazz));
   }
   
   /**
    * 
    * @return
    * @deprecated
    */
   // TODO: figure out permanent solution 
   public ContainerConfiguration getActiveContainerConfiguration() 
   {
      Iterator<Entry<Class<? extends ContainerConfiguration>, ContainerConfiguration>> itr = containersConfig.entrySet().iterator();
      if(itr.hasNext()) 
      {
         return itr.next().getValue();
      }
      return null;
   }
   
   /**
    * Sets the Path used to export deployments.
    *  
    * @param deploymentExportPath String representation of path to use to export archives
    */
   public void setDeploymentExportPath(String deploymentExportPath)
   {
      this.deploymentExportPath = deploymentExportPath;
   }
   
   /**
    * Get the set export path for deployments. 
    * @return Set path or null if not set
    */
   public String getDeploymentExportPath()
   {
      return deploymentExportPath;
   }
}
