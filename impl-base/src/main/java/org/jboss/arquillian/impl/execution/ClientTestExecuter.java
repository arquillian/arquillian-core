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
package org.jboss.arquillian.impl.execution;

import org.jboss.arquillian.api.DeploymentTarget;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.test.DeploymentTargetDescription;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.container.execution.RemoteExecutionEvent;
import org.jboss.arquillian.spi.event.container.execution.LocalExecutionEvent;
import org.jboss.arquillian.spi.event.suite.Test;

/**
 * TestExecuter for running on the client side. Can switch between Local and Remote test execution.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClientTestExecuter
{
   @Inject
   private Instance<ContainerContext> containerContextProvider;

   @Inject
   private Instance<DeploymentContext> deploymentContextProvider;

   @Inject
   private Event<LocalExecutionEvent> localExecutionEvent;
   
   @Inject
   private Event<RemoteExecutionEvent> containerExecutionEvent;

   @Inject
   private Instance<ContainerRegistry> containerRegistry;

   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   public void execute(@Observes Test event) throws Exception
   {
      ContainerContext containerContext = containerContextProvider.get();
      DeploymentContext deploymentContext = deploymentContextProvider.get();
      
      // TODO : move as a abstract/SPI on TestMethodExecutor
      DeploymentTargetDescription target = null;
      if(event.getTestMethod().isAnnotationPresent(DeploymentTarget.class))
      {
         target = new DeploymentTargetDescription(event.getTestMethod().getAnnotation(DeploymentTarget.class).value());
      }
      else
      {
         target = DeploymentTargetDescription.DEFAULT;
      }
      
      DeploymentScenario scenario = deploymentScenario.get();
      
      try
      {
         DeploymentDescription deployment = scenario.getDeployment(target);

         Container container = containerRegistry.get().getContainer(deployment.getTarget());
         containerContext.activate(container.getName());

         try
         {
            // TODO: split up local vs remote execution in two handlers, fire a new set of events LocalExecute RemoteExecute
            deploymentContext.activate(deployment);
            if(scenario.getRunMode() == RunModeType.AS_CLIENT) // TODO: DeploymentScenario should not depend on RunModeType API
            {
               localExecutionEvent.fire(new LocalExecutionEvent(event.getTestMethodExecutor()));
            }
            else
            {
               containerExecutionEvent.fire(new RemoteExecutionEvent(event.getTestMethodExecutor()));
            }
         }
         finally
         {
            deploymentContext.deactivate();
         }
      }
      finally 
      {
         containerContext.deactivate();
      }
   }
}