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
import org.jboss.arquillian.impl.ThreadContext;
import org.jboss.arquillian.impl.client.container.event.ContainerControlEvent;
import org.jboss.arquillian.impl.client.container.event.DeploymentEvent;
import org.jboss.arquillian.impl.core.spi.EventContext;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.test.DeploymentTargetDescription;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.After;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.Test;
import org.jboss.arquillian.spi.event.suite.TestEvent;

/**
 * Activates and DeActivates the Container and Deployment contexts.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerDeploymentContextHandler
{
   @Inject 
   private Instance<ContainerContext> containerContext;

   @Inject 
   private Instance<DeploymentContext> deploymentContext;

   @Inject
   private Instance<ContainerRegistry> containerRegistry;

   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   /*
    * Container Level
    * 
    * Activate ContainerContext on all Container Events
    * 
    */
   public void createContainerContext(@Observes EventContext<ContainerControlEvent> context)
   {
      ContainerContext containerContext = this.containerContext.get();
      ContainerControlEvent event = context.getEvent();

      try
      {
         containerContext.activate(event.getContainerName());
         ThreadContext.set(event.getContainer().getClassLoader());

         context.proceed();
      }
      finally
      {
         ThreadContext.reset();
         containerContext.deactivate();
      }
   }
   
   /*
    * Deployment Level
    * 
    * Activate DeploymentContext on all Deployment Events
    */
   public void createDeploymentContext(@Observes EventContext<DeploymentEvent> context)
   {
      DeploymentContext deploymentContext = this.deploymentContext.get();
      try
      {
         DeploymentEvent event = context.getEvent();
         deploymentContext.activate(event.getDeployment());

         context.proceed();
      }
      finally
      {
         deploymentContext.deactivate();
      }
   }
   
   /*
    * Test Level
    * 
    * Activate Container and Deployment context on Before / Test / After events
    */
   public void createBeforeContext(@Observes EventContext<Before> context) 
   {
      createContext(context);
   }

   public void createTestContext(@Observes EventContext<Test> context) 
   {
      createContext(context);
   }

   public void createAfterContext(@Observes EventContext<After> context) 
   {
      createContext(context);
   }

   private void createContext(EventContext<? extends TestEvent> context) 
   {
      try
      {
         lookup(context.getEvent().getTestMethod(), new Activate());
         context.proceed();
      }
      finally
      {
         lookup(context.getEvent().getTestMethod(), new DeActivate());
      }
   }
   
   /*
    * Internal Helpers needed to extract @DeploymentTarget from TestMethod. 
    * 
    * TODO: This should not rely on direct Reflection, but rather access the metadata through some 
    * common metadata layer.
    */

   private void lookup(Method method, ResultCallback callback)
   {
      DeploymentTargetDescription deploymentTarget = locateDeployment(method);
      
      ContainerRegistry containerRegistry = this.containerRegistry.get();
      DeploymentScenario deploymentScenario = this.deploymentScenario.get();
      
      DeploymentDescription deployment = deploymentScenario.getDeployment(deploymentTarget);
      
      Container container = containerRegistry.getContainer(deployment.getTarget());
      
      callback.call(container, deployment);
   }
   
   // TODO: Needs to be extracted into a MetaModel layer. Should not do reflection directly on TestClass/TestMethods
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
      abstract void call(Container container, DeploymentDescription deployment);
   }
   
   private class Activate extends ResultCallback
   {
      @Override
      void call(Container container, DeploymentDescription deployment)
      {
         containerContext.get().activate(container.getName());
         deploymentContext.get().activate(deployment);
      }
   }

   private class DeActivate extends ResultCallback
   {
      @Override
      void call(Container container, DeploymentDescription deployment)
      {
         containerContext.get().deactivate();
         deploymentContext.get().deactivate();
      }
   }
}
