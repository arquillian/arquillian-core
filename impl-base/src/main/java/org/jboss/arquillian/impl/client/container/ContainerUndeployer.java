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
package org.jboss.arquillian.impl.client.container;

import java.util.List;

import org.jboss.arquillian.impl.ThreadContext;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.container.AfterDeploy;
import org.jboss.arquillian.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.shrinkwrap.api.Archive;

/**
 * A Handler for undeploying the generated {@link Archive} from the container. <br/>
 * <br/>
  *  <b>Fires:</b><br/>
 *   {@link BeforeDeploy}<br/>
 *   {@link AfterDeploy}<br/>
 * <br/>
 *  <b>Imports:</b><br/>
 *   {@link DeployableContainer}<br/>
 *   {@link Archive}<br/>
 *   
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * 
 * @see DeployableContainer
 * @see Archive 
 */
public class ContainerUndeployer 
{
   @Inject 
   private Instance<ContainerContext> containerContextProvider;
   
   @Inject 
   private Instance<DeploymentContext> deploymentContextProvider;
   
   @Inject 
   private Instance<ContainerRegistry> registry;

   @Inject
   private Instance<DeploymentScenario> deploymentScenario;
   
   @Inject
   private Event<BeforeUnDeploy> before;
   
   @Inject
   private Event<AfterUnDeploy> after;

   public void undeploy(@Observes AfterClass event) throws Exception
   {
      ContainerContext containerContext = containerContextProvider.get();
      DeploymentContext deploymentContext = deploymentContextProvider.get();

      ContainerRegistry reg = registry.get();
      DeploymentScenario scenario = deploymentScenario.get();
      
      for(TargetDescription target : scenario.getTargets())
      {
         List<DeploymentDescription> startUpDeployments = scenario.getStartupDeploymentsFor(target);
         Container container = reg.getContainer(target);
         
         ThreadContext.set(container.getClassLoader());
         try
         {
            containerContext.activate(container.getName());
            DeployableContainer<?> deployableContainer = container.getDeployableContainer();
            DeploymentDescription[] deployments = startUpDeployments.toArray(new DeploymentDescription[0]);
            
            for(DeploymentDescription deployment : deployments)
            {
               try
               {
                  deploymentContext.activate(deployment);
                  before.fire(new BeforeUnDeploy(deployableContainer, deployment));
                  
                  if(deployment.isArchiveDeployment())
                  {
                     deployableContainer.undeploy(
                           deployment.getTestableArchive() != null ? deployment.getTestableArchive():deployment.getArchive());
                  }
                  else
                  {
                     deployableContainer.undeploy(deployment.getDescriptor());                     
                  }
                  
                  after.fire(new AfterUnDeploy(deployableContainer, deployment));
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
}
