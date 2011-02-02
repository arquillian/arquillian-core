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

import java.util.List;
import java.util.concurrent.Callable;

import org.jboss.arquillian.impl.ThreadContext;
import org.jboss.arquillian.impl.client.container.event.ContainerControlEvent;
import org.jboss.arquillian.impl.client.container.event.DeployDeployment;
import org.jboss.arquillian.impl.client.container.event.DeployManagedDeployments;
import org.jboss.arquillian.impl.client.container.event.UnDeployDeployment;
import org.jboss.arquillian.impl.client.container.event.UnDeployManagedDeployments;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.DeploymentScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.container.AfterDeploy;
import org.jboss.arquillian.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.spi.event.container.DeployerEvent;

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
   private Instance<ContainerContext> containerContext;

   @Inject 
   private Instance<DeploymentContext> deploymentContext;

   @Inject
   private Instance<ContainerRegistry> containerRegistry;
   
   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   @Inject
   private Instance<Injector> injector;

   public void deployManaged(@Observes DeployManagedDeployments event) throws Exception
   {
      forEachDeployment(new Operation<Container, DeploymentDescription>()
      {
         @Inject 
         private Event<ContainerControlEvent> controllEvent;
         
         @Override
         public void perform(Container container, DeploymentDescription deployment) throws Exception
         {
            controllEvent.fire(new DeployDeployment(container.getDeployableContainer(), deployment));            
         }
      });
   }

   public void undeployManaged(@Observes UnDeployManagedDeployments event) throws Exception
   {
      forEachDeployment(new Operation<Container, DeploymentDescription>()
      {
         @Inject 
         private Event<ContainerControlEvent> controllEvent;
         
         @Override
         public void perform(Container container, DeploymentDescription deployment) throws Exception
         {
            controllEvent.fire(new UnDeployDeployment(container.getDeployableContainer(), deployment));            
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
         private InstanceProducer<DeploymentDescription> deploymentDescription;
         
         @Inject @DeploymentScoped
         private InstanceProducer<ProtocolMetaData> protocolMetadata;

         @Override
         public Void call() throws Exception
         {
            DeployableContainer<?> deployableContainer = event.getDeployableContainer();
            DeploymentDescription deployment = event.getDeployment();
            deploymentDescription.set(deployment);
            
            deployEvent.fire(new BeforeDeploy(deployableContainer, deployment));

            if(deployment.isArchiveDeployment())
            {
               protocolMetadata.set(deployableContainer.deploy(
                     deployment.getTestableArchive() != null ? deployment.getTestableArchive():deployment.getArchive()));
            }
            else
            {
               deployableContainer.deploy(deployment.getDescriptor());
            }
            
            deployEvent.fire(new AfterDeploy(deployableContainer, deployment));
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
            DeploymentDescription deployment = event.getDeployment();
            
            deployEvent.fire(new BeforeUnDeploy(deployableContainer, deployment));

            if(deployment.isArchiveDeployment())
            {
               try
               {
                  deployableContainer.undeploy(
                        deployment.getTestableArchive() != null ? deployment.getTestableArchive():deployment.getArchive());
               }
               catch (Exception e) 
               {
                  // silently ignore UnDeploy exceptions if a expected exception during deploy(it is probably not deployed) 
                  if(deployment.getExpectedException() == null)
                  {
                     throw e;
                  }
               }
            }
            else
            {
               deployableContainer.undeploy(deployment.getDescriptor());
            }
            
            deployEvent.fire(new AfterUnDeploy(deployableContainer, deployment));
            return null;
         }
      });
   }
   
   private void forEachDeployment(Operation<Container, DeploymentDescription> operation) throws Exception
   {
      injector.get().inject(operation);
      
      ContainerContext containerContext = this.containerContext.get();
      DeploymentContext deploymentContext = this.deploymentContext.get();

      ContainerRegistry containerRegistry = this.containerRegistry.get();
      DeploymentScenario deploymentScenario = this.deploymentScenario.get();
      
      for(TargetDescription target : deploymentScenario.getTargets())
      {
         List<DeploymentDescription> startUpDeployments = deploymentScenario.getStartupDeploymentsFor(target);
         if(startUpDeployments.size() == 0)
         {
            continue; // nothing to do, move on 
         }
         
         // Container should exists, handled by up front validation
         Container container = containerRegistry.getContainer(target);
         
         ThreadContext.set(container.getClassLoader());
         try
         {
            containerContext.activate(container.getName());
            for(DeploymentDescription deployment : startUpDeployments)
            {
               try
               {
                  deploymentContext.activate(deployment);
               
                  operation.perform(container, deployment);
               }
               finally
               {
                  deploymentContext.deactivate();
               }
            }
         } 
         finally 
         {
            containerContext.deactivate();
            ThreadContext.reset();
         }
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
