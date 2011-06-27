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
package org.jboss.arquillian.container.impl.client.container;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.GroupDef;
import org.jboss.arquillian.config.descriptor.impl.ContainerDefImpl;
import org.jboss.arquillian.container.impl.LocalContainerRegistry;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * ContainerRegistryCreator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerRegistryCreator
{
   static final String ARQUILLIAN_LAUNCH_PROPERTY = "arquillian.launch";
   static final String ARQUILLIAN_LAUNCH_DEFAULT = "arquillian.launch";

   private Logger log = Logger.getLogger(ContainerRegistryCreator.class.getName());
   
   @Inject @ApplicationScoped
   private InstanceProducer<ContainerRegistry> registry;
   
   @Inject 
   private Instance<Injector> injector;
   
   @Inject
   private Instance<ServiceLoader> loader;

   public void createRegistry(@Observes ArquillianDescriptor event)
   {
      LocalContainerRegistry reg = new LocalContainerRegistry(injector.get());
      ServiceLoader serviceLoader = loader.get();

      validateConfiguration(event);

      String activeConfiguration = getActivatedConfiguration();
      for(ContainerDef container : event.getContainers())
      {
         if(
               (activeConfiguration != null && activeConfiguration.equals(container.getContainerName())) ||
               (activeConfiguration == null && container.isDefault()))
         {
            reg.create(container, serviceLoader);            
         }
      }
      for(GroupDef group : event.getGroups())
      {
         if(
               (activeConfiguration != null && activeConfiguration.equals(group.getGroupName())) ||
               (activeConfiguration == null && group.isGroupDefault()))
         {
            for(ContainerDef container : group.getGroupContainers())
            {
               reg.create(container, serviceLoader);
            }
         }
      }
      if(activeConfiguration == null && reg.getContainers().size() == 0)
      {
         try
         {
            DeployableContainer<?> deployableContainer = serviceLoader.onlyOne(DeployableContainer.class);
            if(deployableContainer != null)
            {
               reg.create(new ContainerDefImpl("arquillian.xml").setContainerName("default"), serviceLoader);
            }
         } 
         catch (Exception e) 
         {
            // ignore
         }
      }

      // export
      registry.set(reg);
   }
   
   /**
    * Validate that the Configuration given is sane
    *
    * @param desc The read Descriptor
    */
   private void validateConfiguration(ArquillianDescriptor desc)
   {
      Object defaultConfig = null;

      // verify only one container is marked as default
      for(ContainerDef container : desc.getContainers())
      {
         if(container.isDefault())
         {
            if(defaultConfig != null)
            {
               throw new IllegalStateException("Multiple Containers defined as default, only one is allowed:\n" + defaultConfig + ":" + container);
            }
            defaultConfig = container;
         }
      }
      boolean containerMarkedAsDefault = defaultConfig != null;

      // verify only one container or group is marked as default
      for(GroupDef group : desc.getGroups())
      {
         if(group.isGroupDefault())
         {
            if(defaultConfig != null)
            {
               if(containerMarkedAsDefault)
               {
                  throw new IllegalStateException("Multiple Containers/Groups defined as default, only one is allowed:\n" + defaultConfig + ":" + group);
               }
               throw new IllegalStateException("Multiple Groups defined as default, only one is allowed:\n" + defaultConfig + ":" + group);
            }
            defaultConfig = group;
         }

         ContainerDef defaultInGroup = null;
         // verify only one container in group is marked as default
         for(ContainerDef container: group.getGroupContainers())
         {
            if(container.isDefault())
            {
               if(defaultInGroup != null)
               {
                  throw new IllegalStateException("Multiple Containers within Group defined as default, only one is allowed:\n" + group);
               }
               defaultInGroup = container;
            }
         }
      }
   }

   private String getActivatedConfiguration() 
   {
      try
      {
         if(exists(System.getProperty(ARQUILLIAN_LAUNCH_PROPERTY)))
         {
            return System.getProperty(ARQUILLIAN_LAUNCH_PROPERTY);
         }
         
         InputStream arquillianLaunchStream = Thread.currentThread().getContextClassLoader()
                                                   .getResourceAsStream(ARQUILLIAN_LAUNCH_DEFAULT);
         if(arquillianLaunchStream != null)
         {
            return readActivatedValue(new BufferedReader(new InputStreamReader(arquillianLaunchStream)));
         }
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
      try
      {
         String value;
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

   private boolean exists(String value)
   {
      if(value == null || value.trim().length() == 0)
      {
         return false;
      }
      return true;
   }
}
