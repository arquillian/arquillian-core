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
package org.jboss.arquillian.impl.client;

import java.lang.reflect.Method;

import org.jboss.arquillian.api.DeploymentTarget;
import org.jboss.arquillian.impl.client.event.ActivateContainerDeploymentContext;
import org.jboss.arquillian.impl.client.event.DeActivateContainerDeploymentContext;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.test.DeploymentTargetDescription;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;

/**
 * Activates and DeActivates the Container and Deployment contexts.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerDeploymentContextHandler
{

   @Inject
   private Instance<ContainerRegistry> containerRegistry;

   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   @Inject
   private Instance<Injector> injector;

   public void activate(@Observes ActivateContainerDeploymentContext event)
   {
      lookup(event.getTestLifecycle().getTestMethod(), new ResultCallback()
      {
         @Override
         public void call(Container container, DeploymentDescription deployment)
         {
            containerContext.get().activate(container.getName());
            deploymentContext.get().activate(deployment);
         }
      });
   }
   
   public void deactivate(@Observes DeActivateContainerDeploymentContext event)
   {
      lookup(event.getTestLifecycle().getTestMethod(), new ResultCallback()
      {
         @Override
         public void call(Container container, DeploymentDescription deployment)
         {
            containerContext.get().deactivate();
            deploymentContext.get().deactivate();
         }
      });
   }

   private void lookup(Method method, ResultCallback callback)
   {
      injector.get().inject(callback);
      DeploymentTargetDescription deploymentTarget = locateDeployment(method);
      
      ContainerRegistry containerRegistry = this.containerRegistry.get();
      DeploymentScenario deploymentScenario = this.deploymentScenario.get();
      
      DeploymentDescription deployment = deploymentScenario.getDeployment(deploymentTarget);
      
      Container container = containerRegistry.getContainer(deployment.getTarget());
      
      callback.call(container, deployment);
   }
   
   private DeploymentTargetDescription locateDeployment(Method method)
   {
      DeploymentTargetDescription target = null;
      if(method.isAnnotationPresent(DeploymentTarget.class))
      {
         target = new DeploymentTargetDescription(method.getAnnotation(DeploymentTarget.class).value());
      }
      else
      {
         target = DeploymentTargetDescription.DEFAULT;
      }
      return target;
   }
   
   private abstract class ResultCallback
   {
      @Inject
      protected Instance<ContainerContext> containerContext;

      @Inject
      protected Instance<DeploymentContext> deploymentContext;

      abstract void call(Container container, DeploymentDescription deployment);
   }
}
