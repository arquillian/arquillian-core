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

import org.jboss.arquillian.api.Deployer;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.DeploymentEvent;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.deployment.DeploymentTargetDescription;

/**
 * ClientDeployer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClientDeployer implements Deployer
{
   @Inject
   private Event<DeploymentEvent> event;
   
   @Inject
   private Instance<ContainerRegistry> containerRegistry;
   
   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Deployer#deploy(java.lang.String)
    */
   @Override
   public void deploy(String name)
   {
      DeploymentScenario scenario = deploymentScenario.get();
      ContainerRegistry registry = containerRegistry.get();
      
      Deployment deployment = scenario.getDeployment(new DeploymentTargetDescription(name));
      if(deployment == null)
      {
         throw new IllegalArgumentException("No deployment in context found with name " + name);
      }
      
      Container container = registry.getContainer(deployment.getDescription().getTarget());
      
      event.fire(new DeployDeployment(container, deployment));
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.api.Deployer#undeploy(java.lang.String)
    */
   @Override
   public void undeploy(String name)
   {
      DeploymentScenario scenario = deploymentScenario.get();
      ContainerRegistry registry = containerRegistry.get();
      
      Deployment deployment = scenario.getDeployment(new DeploymentTargetDescription(name));
      if(deployment == null)
      {
         throw new IllegalArgumentException("No deployment in context found with name " + name);
      }
      Container container = registry.getContainer(deployment.getDescription().getTarget());
      
      event.fire(new UnDeployDeployment(container, deployment));
   }

}
