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
package org.jboss.arquillian.impl;

import org.jboss.arquillian.impl.container.ContainerController;
import org.jboss.arquillian.impl.container.ContainerDeployer;
import org.jboss.arquillian.impl.container.Controlable;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.util.DeployableContainers;
import org.jboss.arquillian.spi.util.TestEnrichers;
import org.jboss.shrinkwrap.api.Archive;

/**
 * DeployableTestBuilder
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeployableTestBuilder
{
   private DeployableTestBuilder() {}
   
   // TODO: lookup/load container, setup DeployableTest
   public static DeployableTest build(Object config) 
   {
      Controlable controller = null;
      Deployer deployer = null;
      
      if(DeployableTest.isInContainer()) 
      {
         controller = new InContainerContainer();
         deployer = new InContainerContainer();
      }
      else 
      {
         DeployableContainer container = DeployableContainers.load();
         controller = new ContainerController(container);
         deployer = new ContainerDeployer(container);
      }

      return new DeployableTest(
            controller,
            deployer
            );
   }
   
   private static class InContainerContainer implements Controlable, Deployer 
   {
      @Override
      public void start() throws LifecycleException
      {
      }

      @Override
      public void stop() throws LifecycleException
      {
      }

      @Override
      public ContainerMethodExecutor deploy(Archive<?> archive) throws DeploymentException
      {
         return new ContainerMethodExecutor()
         {
            @Override
            public TestResult invoke(TestMethodExecutor testMethodExecutor)
            {
               try 
               {
                  TestEnrichers.enrich(testMethodExecutor.getInstance());
                  testMethodExecutor.invoke();
                  return new TestResultImpl(Status.PASSED);
               } 
               catch (Throwable e) 
               {
                  return new TestResultImpl(Status.FAILED, e);
               }
            }
         };
      }

      @Override
      public void undeploy(Archive<?> archive) throws DeploymentException
      {
      }
   }
}
