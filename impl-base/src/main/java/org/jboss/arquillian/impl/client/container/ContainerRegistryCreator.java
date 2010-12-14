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
package org.jboss.arquillian.impl.client.container;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.jboss.arquillian.impl.configuration.ContainerDefImpl;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.configuration.api.ContainerDef;
import org.jboss.arquillian.impl.configuration.api.GroupDef;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;

/**
 * ContainerRegistryCreator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerRegistryCreator
{
   private static final String ARQUILLIAN_LAUNCH_FILE = "arquillian.launch";

   private Logger log = Logger.getLogger(ContainerRegistryCreator.class.getName());
   
   @Inject @ApplicationScoped
   private InstanceProducer<ContainerRegistry> registry;
   
   @Inject
   private Instance<ServiceLoader> loader;

   public void createRegistry(@Observes ArquillianDescriptor event)
   {
      ContainerRegistry reg = new ContainerRegistry();
      ServiceLoader serviceLoader = loader.get();

      String activeConfiguration = getActivatedConfiguration();
      if(activeConfiguration != null)
      {
         for(ContainerDef container : event.getContainers())
         {
            if(activeConfiguration.equals(container.getContainerName()))
            {
               reg.create(container, serviceLoader);            
            }
         }
         for(GroupDef group : event.getGroups())
         {
            if(activeConfiguration.equals(group.getGroupName()))
            {
               for(ContainerDef container : group.getContainers())
               {
                  reg.create(container, serviceLoader);
               }
            }
         }
      }
      else
      {
         try
         {
            DeployableContainer<?> deployableContainer = serviceLoader.onlyOne(DeployableContainer.class);
            if(deployableContainer != null)
            {
               reg.create(new ContainerDefImpl().setContainerName("default"), serviceLoader);
            }
         } 
         catch (Exception e) 
         {
            // ugnore
         }
      }

      // export
      registry.set(reg);
   }
   
   private String getActivatedConfiguration() 
   {
      try
      {
         return readActivatedValue(
               new BufferedReader(
                     new InputStreamReader(
                           Thread.currentThread().getContextClassLoader().getResourceAsStream(ARQUILLIAN_LAUNCH_FILE))));
         
      }
      catch (Exception e) 
      {
         log.info("Could not read active container configuration: " + e.getMessage());
      }
      return null;
   }
   
   private String readActivatedValue(BufferedReader reader)
      throws Exception
   {
      String value;
      try
      {
         while( (value = reader.readLine()) != null)
         {
            if(value.startsWith("#"))
            {
               continue;
            }
            return value;
         }
      }
      finally
      {
         reader.close();
      }
      return null;
   }
}
