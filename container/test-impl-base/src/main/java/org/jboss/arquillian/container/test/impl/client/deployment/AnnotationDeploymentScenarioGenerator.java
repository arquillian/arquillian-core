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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ExcludeServices;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ServiceType;
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
      
      Field[] deploymentFields = testClass.getFields(Deployment.class);
      for (Field deploymentField : deploymentFields)
      {
         validate(deploymentField);
         deployments.add(generateDeployment(deploymentField));
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
   
   private void validate(Field deploymentField)
   {
      if(!Modifier.isStatic(deploymentField.getModifiers()))
      {
         throw new IllegalArgumentException("Field annotated with " + Deployment.class.getName() + " is not static. "  + deploymentField);
      }
      if(!Archive.class.isAssignableFrom(deploymentField.getType()) && !Descriptor.class.isAssignableFrom(deploymentField.getType())) 
      {
         throw new IllegalArgumentException(
               "Field annotated with " + Deployment.class.getName() + 
               " must be of type " + Archive.class.getName() +  " or " + Descriptor.class.getName() + ". " + deploymentField);
      }
   }
   
   /**
    * @param deploymentMember
    * @return
    */
   private DeploymentDescription generateDeployment(AccessibleObject deploymentMember)
   {
      TargetDescription target = generateTarget(deploymentMember);
      ProtocolDescription protocol = generateProtocol(deploymentMember);
      
      Deployment deploymentAnnotation = deploymentMember.getAnnotation(Deployment.class);
      DeploymentDescription deployment = null;
      Class<?> type = getType(deploymentMember);
      if(Archive.class.isAssignableFrom(type))
      {
         deployment = new DeploymentDescription(deploymentAnnotation.name(), getOrInvoke(Archive.class, deploymentMember));
         ExcludeServices excludeServicesAnnotation = deploymentMember.getAnnotation(ExcludeServices.class);
         if (excludeServicesAnnotation != null) {
             List<String> exclusions = Arrays.asList(excludeServicesAnnotation.value());
             deployment.shouldExcludeServices(exclusions);
             if (exclusions.contains(ServiceType.ALL) || exclusions.contains(ServiceType.TEST_RUNNER)) {
                 deployment.shouldBeTestable(false);
             }
         }
         else {
             deployment.shouldBeTestable(deploymentAnnotation.testable());
         }
      }
      else if(Descriptor.class.isAssignableFrom(type))
      {
         deployment = new DeploymentDescription(deploymentAnnotation.name(), getOrInvoke(Descriptor.class, deploymentMember));
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
      
      if(deploymentMember.isAnnotationPresent(ShouldThrowException.class))
      {
         deployment.setExpectedException(deploymentMember.getAnnotation(ShouldThrowException.class).value());
         deployment.shouldBeTestable(false); // can't test against failing deployments
      }
      
      return deployment;
   }
   
   /**
    * @param deploymentMethod
    * @return
    */
   private TargetDescription generateTarget(AccessibleObject deploymentMethod)
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
   private ProtocolDescription generateProtocol(AccessibleObject deploymentMethod)
   {
      if(deploymentMethod.isAnnotationPresent(OverProtocol.class))
      {
         return new ProtocolDescription(deploymentMethod.getAnnotation(OverProtocol.class).value());
      }
      return ProtocolDescription.DEFAULT;
   }

   private <T> T getOrInvoke(Class<T> type, AccessibleObject deploymentMember)
   {
      if (deploymentMember instanceof Method)
      {
         return invoke(type, (Method) deploymentMember);
      }
      else
      {
         return get(type, (Field) deploymentMember); 
      }
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
  
   /**
    * @param deploymentField
    * @return
    */
   private <T> T get(Class<T> type, Field deploymentField)
   {
      try
      {
         return type.cast(deploymentField.get(null));
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not get value of deployment field: " + deploymentField, e);
      }
   }
   
   private Class<?> getType(AccessibleObject member)
   {
      if (member instanceof Method)
      {
         return Method.class.cast(member).getReturnType();
      }
      else
      {
         return Field.class.cast(member).getType();
      }
   }
   
}
