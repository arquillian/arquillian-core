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
package org.jboss.arquillian.container.test.impl.client.container;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentTargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.event.ContainerControlEvent;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.KillContainer;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;

/**
 * ClientContainerController
 * 
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 * 
 */
public class ClientContainerController implements ContainerController
{

   private final Logger log = Logger.getLogger(ClientContainerController.class.getName());

   @Inject
   private Event<ContainerControlEvent> event;

   @Inject
   private Instance<ContainerRegistry> containerRegistry;
   
   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   @Override
   public void start(String containerQualifier)
   {
      DeploymentScenario scenario = deploymentScenario.get();
      if(scenario == null)
      {
         throw new IllegalArgumentException("No deployment scenario in context");
      }
      
      ContainerRegistry registry = containerRegistry.get();
      if (registry == null)
      {
         throw new IllegalArgumentException("No container registry in context");
      }

      if (!containerExists(registry.getContainers(), containerQualifier))
      {
         throw new IllegalArgumentException("No container with the specified name exists");
      }

      if (!isManualContainer(registry.getContainers(), containerQualifier))
      {
         throw new IllegalArgumentException("Could not start " + containerQualifier + " container. The container life cycle is controlled by Arquillian");
      }
      
      List<Deployment> managedDeployments = scenario.startupDeploymentsFor(new TargetDescription(containerQualifier));
      
      Container container = registry.getContainer(new TargetDescription(containerQualifier));

      log.info("Manual starting of a server instance");

      event.fire(new StartContainer(container));
      
      for (Deployment d : managedDeployments)
      {
         if (!d.isDeployed()) 
         {
            log.info("Automatic deploying of the managed deployment with name " + d.getDescription().getName() + 
               " for the container with name " + container.getName());
            event.fire(new DeployDeployment(container, d));
         }
      }
   }

   @Override
   public void start(String containerQualifier, Map<String, String> config)
   {
      DeploymentScenario scenario = deploymentScenario.get();
      if(scenario == null)
      {
         throw new IllegalArgumentException("No deployment scenario in context");
      }
      
      ContainerRegistry registry = containerRegistry.get();
      if (registry == null)
      {
         throw new IllegalArgumentException("No container registry in context");
      }

      if (!containerExists(registry.getContainers(), containerQualifier))
      {
         throw new IllegalArgumentException("No container with the specified name exists");
      }
      
      if (!isManualContainer(registry.getContainers(), containerQualifier))
      {
         throw new IllegalArgumentException("Could not start " + containerQualifier + " container. The container life cycle is controlled by Arquillian");
      }

      List<Deployment> managedDeployments = scenario.startupDeploymentsFor(new TargetDescription(containerQualifier));
      
      Container container = registry.getContainer(new TargetDescription(containerQualifier));

      for (String name : config.keySet())
      {
         container.getContainerConfiguration().overrideProperty(name, config.get(name));
      }

      log.info("Manual starting of a server instance with overridden configuration. New configuration: " + 
         container.getContainerConfiguration().getContainerProperties());

      event.fire(new SetupContainer(container));
      event.fire(new StartContainer(container));
      
      for (Deployment d : managedDeployments)
      {
         if (!d.isDeployed()) 
         {
            log.info("Automatic deploying of the managed deployment with name " + d.getDescription().getName() + 
               " for the container with name " + container.getName());
            event.fire(new DeployDeployment(container, d));
         }
      }
   }

   @Override
   public void stop(String containerQualifier)
   {
      DeploymentScenario scenario = deploymentScenario.get();
      if(scenario == null)
      {
         throw new IllegalArgumentException("No deployment scenario in context");
      }
      
      ContainerRegistry registry = containerRegistry.get();
      if (registry == null)
      {
         throw new IllegalArgumentException("No container registry in context");
      }

      if (!containerExists(registry.getContainers(), containerQualifier))
      {
         throw new IllegalArgumentException("No container with the specified name exists");
      }
      
      if (!isManualContainer(registry.getContainers(), containerQualifier))
      {
         throw new IllegalArgumentException("Could not start " + containerQualifier + " container. The container life cycle is controlled by Arquillian");
      }

      Container container = registry.getContainer(new TargetDescription(containerQualifier));

      List<Deployment> managedDeployments = scenario.startupDeploymentsFor(new TargetDescription(containerQualifier));
      
      for (Deployment d : managedDeployments)
      {
         if (d.isDeployed()) 
         {
            log.info("Automatic undeploying of the managed deployment with name " + d.getDescription().getName() + 
               " from the container with name " + container.getName());
            event.fire(new UnDeployDeployment(container, d));
         }
      }
      
      log.info("Manual stopping of a server instance");

      event.fire(new StopContainer(container));
   }

   @Override
   public void kill(String containerQualifier)
   {
      ContainerRegistry registry = containerRegistry.get();
      if (registry == null)
      {
         throw new IllegalArgumentException("No container registry in context");
      }

      if (!containerExists(registry.getContainers(), containerQualifier))
      {
         throw new IllegalArgumentException("No container with the specified name exists");
      }
      
      if (!isManualContainer(registry.getContainers(), containerQualifier))
      {
         throw new IllegalArgumentException("Could not start " + containerQualifier + " container. The container life cycle is controlled by Arquillian");
      }

      Container container = registry.getContainer(new TargetDescription(containerQualifier));

      log.info("Hard killing of a server instance");

      event.fire(new KillContainer(container));
   }

   private boolean containerExists(List<Container> containers, String name)
   {
      for (Container container : containers)
      {
         if (container.getName().equals(name))
         {
            return true;
         }
      }
      return false;
   }

   private boolean isManualContainer(List<Container> containers, String containerQualifier)
   {
      for (Container container : containers)
      {
         if (container.getName().equals(containerQualifier) && "manual".equals(container.getContainerConfiguration().getMode()))
         {
            return true;
         }
      }
      return false;
   }
}
