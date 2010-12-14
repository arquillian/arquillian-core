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
package org.jboss.arquillian.impl.client.protocol;

import org.jboss.arquillian.api.DeploymentTarget;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.protocol.Protocol;
import org.jboss.arquillian.spi.client.protocol.ProtocolConfiguration;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.test.DeploymentTargetDescription;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.core.annotation.TestScoped;
import org.jboss.arquillian.spi.event.suite.Test;

/**
 * A Handler for executing the remote Test Method.<br/>
 * <br/>
 *  <b>Imports:</b><br/>
 *   {@link DeployableContainer}<br/>
 *  <br/>
 *  <b>Exports:</b><br/>
 *   {@link TestResult}<br/>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * @see DeployableContainer
 */
public class RemoteTestExecuter
{
   @Inject
   private Instance<DeploymentScenario> deploymentScenario;

   @Inject
   private Instance<ProtocolRegistry> protocolRegistry;

   @Inject
   private Instance<ContainerRegistry> containerRegistry;

   @Inject
   private Instance<ProtocolMetaData> protocolMetadata;

   @Inject
   private Instance<ContainerContext> containerContextProvider;

   @Inject
   private Instance<DeploymentContext> deploymentContextProvider;

   @Inject @TestScoped
   private InstanceProducer<TestResult> testResult;

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
               testResult.set(executeLocal(event));
            }
            else
            {
               testResult.set(executeRemote(event, deployment, container));
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

   private TestResult executeRemote(Test event, DeploymentDescription deployment, Container container) throws Exception
   {
      ProtocolRegistry protoReg = protocolRegistry.get();

      // if no default marked or specific protocol defined in the registry, use the DeployableContainers defaultProtocol.
      ProtocolDefinition protocol = protoReg.getProtocol(deployment.getProtocol());
      if(protocol == null)
      {
         protocol = protoReg.getProtocol(container.getDeployableContainer().getDefaultProtocol());
      }
    
      ProtocolConfiguration protocolConfiguration;
      
      if(container.hasProtocolConfiguration(protocol.getProtocolDescription()))
      {
         protocolConfiguration = protocol.createProtocolConfiguration(
               container.getProtocolConfiguration(protocol.getProtocolDescription()).getProtocolProperties());
      } 
      else
      {
         protocolConfiguration = protocol.createProtocolConfiguration();
      }
      // TODO: cast to raw type to get away from generic issue.. 
      ContainerMethodExecutor executor = ((Protocol)protocol.getProtocol()).getExecutor(protocolConfiguration, protocolMetadata.get());
      return executor.invoke(event.getTestMethodExecutor());
   }

   /**
    * 
    */
   private TestResult executeLocal(Test event)
   {
      TestResult result = new TestResult();
      try 
      {
         event.getTestMethodExecutor().invoke();
         result.setStatus(Status.PASSED);
      } 
      catch (Throwable e) 
      {
         result.setStatus(Status.FAILED);
         result.setThrowable(e);
      }
      finally 
      {
         result.setEnd(System.currentTimeMillis());         
      }
      return result;
   }
}
