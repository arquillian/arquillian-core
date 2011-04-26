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

import java.util.Collection;

import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;

/**
 * A exception handler that attempt to Veto a Exception during deployment if {@link ShouldThrowException} is defined for the {@link Deployment}. 
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentExceptionHandler
{
   @Inject 
   private Instance<ServiceLoader> serviceLoader;
   
   public void verifyExpectedExceptionDuringDeploy(@Observes EventContext<DeployDeployment> context) throws Exception
   {
      DeploymentDescription deployment = context.getEvent().getDeployment().getDescription(); 
      boolean deploymentExceptionThrown = true;
      try
      {
         context.proceed();
         if(deployment.getExpectedException() != null)
         {
            deploymentExceptionThrown = false;
            throw new RuntimeException(
                  "Expected exception of type " + deployment.getExpectedException().getName() + " during deployment of " + 
                  deployment.getName() + ", but no exception was thrown.");
         }
      }
      catch (Exception e) 
      {
         // if exception is not thrown from the deployment, rethrow it (we threw it).
         if(!deploymentExceptionThrown)
         {
            throw e;
         }
         if(deployment.getExpectedException() != null)
         {
            if(!containsType(
                  transform(e), 
                  deployment.getExpectedException()))
            {
               throw e;
            }
         }
         else
         {
            throw e;
         }
      }
   }
   
/*
   public void verifyExpectedExceptionDuringUnDeploy(@Observes EventContext<UnDeployDeployment> context) throws Exception
   {
      DeploymentDescription deployment = context.getEvent().getDeployment(); 
      try
      {
         context.proceed();
      }
      catch (Exception e) 
      {
         if(deployment.getExpectedException() == null)
         {
               throw e;
         }
      }
   }
*/
   
   private boolean containsType(Throwable exception, Class<? extends Exception> expectedType)
   {
      if(exception == null)
      {
         return false;
      }
      if(expectedType.isAssignableFrom(exception.getClass()))
      {
         return true;
      }
      
      return containsType(exception.getCause(), expectedType);
   }
   
   private Throwable transform(Throwable exception)
   {
      Throwable toBeTransformed = exception;
      if(exception instanceof DeploymentException)
      {
         toBeTransformed = exception.getCause();
      }
      Collection<DeploymentExceptionTransformer> transformers = serviceLoader.get().all(DeploymentExceptionTransformer.class);
      for(DeploymentExceptionTransformer transformer : transformers)
      {
         Throwable transformed = transformer.transform(toBeTransformed);
         if(transformed != null)
         {
            return transformed;
         }
      }
      return toBeTransformed;
   }
}
