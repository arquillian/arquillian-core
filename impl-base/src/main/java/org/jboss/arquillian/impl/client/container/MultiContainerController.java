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
package org.jboss.arquillian.impl.client.container;


/**
 * TargetedDeployableContainer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class MultiContainerController
{
//   /**
//    * Setup all {@link DeployableContainer}s returned by the {@link ContainerRegistry}.
//    * 
//    * @param context
//    * @param configuration
//    * @return
//    */
//   public ContainerOperationResult<Void> setup(Context context, Configuration configuration)
//   {
//      ContainerOperationResult<Void> result = new ContainerOperationResult<Void>();
//      ContainerRegistry manager = context.get(ContainerRegistry.class);
//      for(Container container : manager.getContainers())
//      {
//         try
//         {
//            container.getDeployableContainer().setup(context, configuration);
//            result.add(setupSuccess(container));
//         }
//         catch (Exception e) 
//         {
//            result.add(setupFailure(container, e));
//         }
//      }
//      return result;
//   }
//
//   /**
//    * Start all {@link DeployableContainer}s returned by the {@link ContainerRegistry}.
//    * 
//    * @param context
//    * @return
//    * @throws LifecycleException
//    */
//   public ContainerOperationResult<Void> start(Context context)
//   {
//      ContainerOperationResult<Void> result = new ContainerOperationResult<Void>();
//      ContainerRegistry manager = context.get(ContainerRegistry.class);
//      for(Container container : manager.getContainers())
//      {
//         try
//         {
//            container.getDeployableContainer().start(context);
//            result.add(startSuccess(container));
//         }
//         catch (Exception e) 
//         {
//            result.add(startFailure(container, e));
//         }
//      }
//      return result;
//   }
//
//   /**
//    * Stop all {@link DeployableContainer}s returned by the {@link ContainerRegistry}
//    * 
//    * @param context
//    * @return
//    * @throws LifecycleException
//    */
//   public ContainerOperationResult<Void> stop(Context context)
//   {
//      ContainerOperationResult<Void> result = new ContainerOperationResult<Void>();
//      ContainerRegistry manager = context.get(ContainerRegistry.class);
//      for(Container container : manager.getContainers())
//      {
//         try
//         {
//            container.getDeployableContainer().start(context);
//            result.add(stopSuccess(container));
//         }
//         catch (Exception e) 
//         {
//            result.add(stopFailure(container, e));
//         }
//      }
//      return result;
//   }
//
//   // TODO: when DeployableContianer opens up for deploy(Deployments..) we should pass the targeted deployments down in order
//   public ContainerOperationResult<DeploymentDescription> deploy(Context context, DeploymentDescription... deployments)
//   {
//      ContainerOperationResult<DeploymentDescription> result = new ContainerOperationResult<DeploymentDescription>();
//      ContainerRegistry manager = context.get(ContainerRegistry.class);
//      for(DeploymentDescription deployment : deployments)
//      {
//         Container container = manager.getContainer(deployment.getTarget());
//         try
//         {
//            container.getDeployableContainer().deploy(context, deployment.getArchive());
//            result.add(deploySuccess(container, deployment));
//         }
//         catch (Exception e) 
//         {
//            result.add(deployFailure(container, deployment, e));
//         }
//      }
//      return result;
//   }
//
//   // TODO: when DeployableContianer opens up for deploy(Deployments.. ) we should pass the targeted deployments down in order
//   public ContainerOperationResult<DeploymentDescription> undeploy(Context context, DeploymentDescription... deployments)
//   {
//      ContainerOperationResult<DeploymentDescription> result = new ContainerOperationResult<DeploymentDescription>();
//      ContainerRegistry manager = context.get(ContainerRegistry.class);
//      for(DeploymentDescription deployment : deployments)
//      {
//         Container container = manager.getContainer(deployment.getTarget());
//         try
//         {
//            container.getDeployableContainer().undeploy(context, deployment.getArchive());
//            result.add(unDeploySuccess(container, deployment));
//         }
//         catch (Exception e) 
//         {
//            result.add(unDeployFailure(container, deployment, e));
//         }
//      }
//      return result;
//   }
}
