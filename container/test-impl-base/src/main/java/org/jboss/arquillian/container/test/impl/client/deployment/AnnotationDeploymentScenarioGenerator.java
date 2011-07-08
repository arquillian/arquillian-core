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
package org.jboss.arquillian.container.test.impl.client.deployment;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * {@link DeploymentScenarioGenerator} that builds a {@link DeploymentScenario} based on 
 * the standard Arquillian API annotations.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class AnnotationDeploymentScenarioGenerator implements DeploymentScenarioGenerator
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.deployment.DeploymentScenarioGenerator#generate(org.jboss.arquillian.spi.TestClass)
    */
   public List<DeploymentDescription> generate(TestClass testClass)
   {
      List<DeploymentDescription> deployments = new ArrayList<DeploymentDescription>();
      Method[] deploymentMethods = testClass.getMethods(Deployment.class);

      for(Method deploymentMethod: deploymentMethods)
      {
         validate(deploymentMethod);
         deployments.add(generateDeployment(deploymentMethod));
      }
      
      return deployments;
   }

   private void validate(Method deploymentMethod)
   {
      if(!Modifier.isStatic(deploymentMethod.getModifiers()))
      {
         throw new IllegalArgumentException("Method annotated with " + Deployment.class.getName() + " is not static. "  + deploymentMethod);
      }
      if(!Archive.class.isAssignableFrom(deploymentMethod.getReturnType()) && !Descriptor.class.isAssignableFrom(deploymentMethod.getReturnType())) 
      {
         throw new IllegalArgumentException(
               "Method annotated with " + Deployment.class.getName() + 
               " must have return type " + Archive.class.getName() +  " or " + Descriptor.class.getName() + ". " + deploymentMethod);
      }
      if(deploymentMethod.getParameterTypes().length != 0)
      {
         throw new IllegalArgumentException("Method annotated with " + Deployment.class.getName() + " can not accept parameters. " + deploymentMethod);         
      }
   }
   
   /**
    * @param deploymentMethod
    * @return
    */
   private DeploymentDescription generateDeployment(Method deploymentMethod)
   {
      TargetDescription target = generateTarget(deploymentMethod);
      ProtocolDescription protocol = generateProtocol(deploymentMethod);
      
      Deployment deploymentAnnotation = deploymentMethod.getAnnotation(Deployment.class);
      DeploymentDescription deployment = null;
      if(Archive.class.isAssignableFrom(deploymentMethod.getReturnType()))
      {
         deployment = new DeploymentDescription(deploymentAnnotation.name(), invoke(Archive.class, deploymentMethod));
         deployment.shouldBeTestable(deploymentAnnotation.testable());
      }
      else if(Descriptor.class.isAssignableFrom(deploymentMethod.getReturnType()))
      {
         deployment = new DeploymentDescription(deploymentAnnotation.name(), invoke(Descriptor.class, deploymentMethod));
         //deployment.shouldBeTestable(false);
      }
      deployment.shouldBeManaged(deploymentAnnotation.managed());
      deployment.setOrder(deploymentAnnotation.order());
      if(target != null)
      {
         deployment.setTarget(target);
      }
      if(protocol != null)
      {
         deployment.setProtocol(protocol);
      }
      
      if(deploymentMethod.isAnnotationPresent(ShouldThrowException.class))
      {
         deployment.setExpectedException(deploymentMethod.getAnnotation(ShouldThrowException.class).value());
         deployment.shouldBeTestable(false); // can't test against failing deployments
      }
      
      return deployment;
   }

   /**
    * @param deploymentMethod
    * @return
    */
   private TargetDescription generateTarget(Method deploymentMethod)
   {
      if(deploymentMethod.isAnnotationPresent(TargetsContainer.class))
      {
         return new TargetDescription(deploymentMethod.getAnnotation(TargetsContainer.class).value());
      }
      return TargetDescription.DEFAULT;
   }

   /**
    * @param deploymentMethod
    * @return
    */
   private ProtocolDescription generateProtocol(Method deploymentMethod)
   {
      if(deploymentMethod.isAnnotationPresent(OverProtocol.class))
      {
         return new ProtocolDescription(deploymentMethod.getAnnotation(OverProtocol.class).value());
      }
      return ProtocolDescription.DEFAULT;
   }

   /**
    * @param deploymentMethod
    * @return
    */
   private <T> T invoke(Class<T> type, Method deploymentMethod)
   {
      try
      {
         return type.cast(deploymentMethod.invoke(null));
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not invoke deployment method: " + deploymentMethod, e);
      }
   }
}
