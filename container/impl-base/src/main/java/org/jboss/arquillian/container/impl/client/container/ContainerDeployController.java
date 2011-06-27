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

import java.util.List;
import java.util.concurrent.Callable;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.Container.State;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.DeploymentEvent;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.container.spi.event.container.DeployerEvent;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Controller for handling all Deployment related operations. <br/>
 * <br/> 
 * 
 * Fires DeployDeployment events for each deployment that should be deployed during startup. This so the Cores exception handling
 * will be triggered if Deployment fails inside the context of the deployment and container. This lets extensions listen for Exceptions types
 * and handle them inside the same context.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerDeployController
{
   @Inject
   private Instance<ContainerRegistry> containerRegistry;
   
   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   @Inject
   private Instance<Injector> injector;

   /**
    * Deploy all deployments marked as managed = true.
    * 
    * @param event
    * @throws Exception
    */
   public void deployManaged(@Observes DeployManagedDeployments event) throws Exception
   {
      forEachManagedDeployment(new Operation<Container, Deployment>()
      {
         @Inject 
         private Event<DeploymentEvent> event;
         
         @Override
         public void perform(Container container, Deployment deployment) throws Exception
         {
            //when a container is manually controlled, the deployment is deployed automatically
            //once the container is manually started, not now
            if (! "manual".equals(container.getContainerConfiguration().getMode()))
            {
               event.fire(new DeployDeployment(container, deployment));
            }
         }
      });
   }

   /**
    * Undeploy all deployments marked as managed, and all manually deployed. 
    * 
    * @param event
    * @throws Exception
    */
   public void undeployManaged(@Observes UnDeployManagedDeployments event) throws Exception
   {
      forEachDeployedDeployment(new Operation<Container, Deployment>()
      {
         @Inject 
         private Event<DeploymentEvent> event;
         
         @Override
         public void perform(Container container, Deployment deployment) throws Exception
         {
            if (container.getState().equals(Container.State.STARTED) && deployment.isDeployed()) 
            {
               event.fire(new UnDeployDeployment(container, deployment));
            }
         }
      });
   }

   public void deploy(@Observes final DeployDeployment event) throws Exception
   {
      executeOperation(new Callable<Void>()
      {
         @Inject
         private Event<DeployerEvent> deployEvent;
         
         @Inject @DeploymentScoped
         private InstanceProducer<DeploymentDescription> deploymentDescriptionProducer;

         @Inject @DeploymentScoped
         private InstanceProducer<Deployment> deploymentProducer;
         
         @Inject @DeploymentScoped
         private InstanceProducer<ProtocolMetaData> protocolMetadata;

         @Override
         public Void call() throws Exception
         {
            DeployableContainer<?> deployableContainer = event.getDeployableContainer();
            Deployment deployment = event.getDeployment();
            DeploymentDescription deploymentDescription = deployment.getDescription();
            
            /*
             * TODO: should the DeploymentDescription producer some how be automatically registered ?
             * Or should we just 'know' who is the first one to create the context
             */
            deploymentDescriptionProducer.set(deploymentDescription);
            deploymentProducer.set(deployment);
            
            deployEvent.fire(new BeforeDeploy(deployableContainer, deploymentDescription));

            try
            {
               if(deploymentDescription.isArchiveDeployment())
               {
                  protocolMetadata.set(deployableContainer.deploy(
                        deploymentDescription.getTestableArchive() != null ? deploymentDescription.getTestableArchive():deploymentDescription.getArchive()));
               }
               else
               {
                  deployableContainer.deploy(deploymentDescription.getDescriptor());
               }
               deployment.deployed();
            }
            catch (Exception e) 
            {
               deployment.deployedWithError(e);
               throw e;
            }
            
            deployEvent.fire(new AfterDeploy(deployableContainer, deploymentDescription));
            return null;
         }
      });
   }
   
   public void undeploy(@Observes final UnDeployDeployment event) throws Exception
   {
      executeOperation(new Callable<Void>()
      {
         @Inject
         private Event<DeployerEvent> deployEvent;

         @Override
         public Void call() throws Exception
         {
            DeployableContainer<?> deployableContainer = event.getDeployableContainer();
            Deployment deployment = event.getDeployment();
            DeploymentDescription description = deployment.getDescription();
            
            deployEvent.fire(new BeforeUnDeploy(deployableContainer, description));

            try
            {
               
               if(deployment.getDescription().isArchiveDeployment())
               {
                  try
                  {
                     deployableContainer.undeploy(
                           description.getTestableArchive() != null ? description.getTestableArchive():description.getArchive());
                  }
                  catch (Exception e) 
                  {
                     if(!deployment.hasDeploymentError())
                     {
                        throw e;
                     }
                  }
               }
               else
               {
                  deployableContainer.undeploy(description.getDescriptor());
               }
            }
            finally
            {
               deployment.undeployed();
            }
            
            deployEvent.fire(new AfterUnDeploy(deployableContainer, description));
            return null;
         }
      });
   }
   
   private void forEachManagedDeployment(Operation<Container, Deployment> operation) throws Exception
   {
      DeploymentScenario scenario = this.deploymentScenario.get();
      if(scenario == null)
      {
         return;
      }
      forEachDeployment(scenario.managedDeploymentsInDeployOrder(), operation);
   }

   private void forEachDeployedDeployment(Operation<Container, Deployment> operation) throws Exception
   {
      DeploymentScenario scenario = this.deploymentScenario.get();
      if(scenario == null)
      {
         return;
      }
      forEachDeployment(scenario.deployedDeploymentsInUnDeployOrder(), operation);
   }

   private void forEachDeployment(List<Deployment> deployments, Operation<Container, Deployment> operation) throws Exception
   {
      injector.get().inject(operation);
      ContainerRegistry containerRegistry = this.containerRegistry.get();
      if(containerRegistry == null)
      {
         return;
      }
      for(Deployment deployment: deployments)
      {
         Container container = containerRegistry.getContainer(deployment.getDescription().getTarget());
         operation.perform(container, deployment);
      }
   }
   
   private void executeOperation(Callable<Void> operation)
      throws Exception
   {
      injector.get().inject(operation);
      operation.call();
   }

   public interface Operation<T, X>
   {
      void perform(T container, X deployment) throws Exception;
   }
}
