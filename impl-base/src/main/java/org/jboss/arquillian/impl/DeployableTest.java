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

import org.jboss.arquillian.impl.container.Controlable;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.Archive;

/**
 * DeployableTest
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeployableTest
{
   // TODO: move to DeploybleTestBuilder
   private static boolean inContainer = false;
   
   public static boolean isInContainer()
   {
      return inContainer;
   }

   public static void setInContainer(boolean inContainer)
   {
      DeployableTest.inContainer = inContainer;
   }

   private Controlable containerController;
   private Deployer containerDeployer;
   private ServiceLoader serviceLoader;
   
   
   DeployableTest(Controlable containerController, Deployer containerDeployer, ServiceLoader serviceLoader)
   {
      Validate.notNull(containerController, "ContainerController must bespecified");
      Validate.notNull(containerDeployer, "ContainerDeployer must bespecified");
      Validate.notNull(serviceLoader, "ServiceLoader must bespecified");
      
      this.containerController = containerController;
      this.containerDeployer = containerDeployer;
      this.serviceLoader = serviceLoader;
   }
   
   public Controlable getContainerController() 
   {
      return containerController;
   }

   public Deployer getDeployer() 
   {
      return containerDeployer;
   }

   public Archive<?> generateArchive(Class<?> testCase) 
   {
      return getArchiveGenerator().generate(testCase);
   }

   // TODO: move DeploymentGenerator injection to DeployableTestBuilder
   private DeploymentGenerator getArchiveGenerator() 
   {
      if(DeployableTest.isInContainer()) 
      {
         return new NullDeploymentGenerator();
      }
      return new ClientDeploymentGenerator(serviceLoader);
   }
}
