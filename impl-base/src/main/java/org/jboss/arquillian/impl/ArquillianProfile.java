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
package org.jboss.arquillian.impl;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.arquillian.impl.bootstrap.ConfigurationRegistrar;
import org.jboss.arquillian.impl.bootstrap.ServiceLoaderRegistrar;
import org.jboss.arquillian.impl.client.ContainerAfterController;
import org.jboss.arquillian.impl.client.ContainerBeforeController;
import org.jboss.arquillian.impl.client.container.ContainerDeployController;
import org.jboss.arquillian.impl.client.container.ContainerLifecycleController;
import org.jboss.arquillian.impl.client.container.ContainerRegistryCreator;
import org.jboss.arquillian.impl.client.container.DeploymentExceptionHandler;
import org.jboss.arquillian.impl.client.deployment.ArchiveDeploymentExporter;
import org.jboss.arquillian.impl.client.deployment.DeploymentGenerator;
import org.jboss.arquillian.impl.client.protocol.ProtocolRegistryCreator;
import org.jboss.arquillian.impl.enricher.ClientTestEnricher;
import org.jboss.arquillian.impl.enricher.ContainerTestEnricher;
import org.jboss.arquillian.impl.execution.AfterLifecycleEventExecuter;
import org.jboss.arquillian.impl.execution.BeforeLifecycleEventExecuter;
import org.jboss.arquillian.impl.execution.ClientTestExecuter;
import org.jboss.arquillian.impl.execution.ContainerTestExecuter;
import org.jboss.arquillian.impl.execution.LocalTestExecuter;
import org.jboss.arquillian.impl.execution.RemoteTestExecuter;
import org.jboss.arquillian.spi.Profile;

/**
 * ArquillianProfile
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianProfile implements Profile
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Profile#getClientProfile()
    */
   @SuppressWarnings("unchecked")
   @Override
   public Collection<Class<?>> getClientProfile()
   {
      return Arrays.asList
      (
            // core
            ServiceLoaderRegistrar.class,
            ConfigurationRegistrar.class,

            // container core
            ProtocolRegistryCreator.class,
            ContainerRegistryCreator.class,
            DeploymentExceptionHandler.class,
            
            // container / deploy / test
            ContainerBeforeController.class,            
            

            ClientTestEnricher.class,
            ClientTestExecuter.class,
            LocalTestExecuter.class,
            RemoteTestExecuter.class,
            
            ContainerAfterController.class,
            
            // container utils
            ArchiveDeploymentExporter.class,

            // core
            
            // container
            ContainerLifecycleController.class,
            ContainerDeployController.class,
            DeploymentGenerator.class
      );
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Profile#getContainerProfile()
    */
   @SuppressWarnings("unchecked")
   @Override
   public Collection<Class<?>> getContainerProfile()
   {
      return Arrays.asList
      (
            // core
            ServiceLoaderRegistrar.class,
            
            // execute the Test lifecycles, we want to execute After before what we do
            AfterLifecycleEventExecuter.class,

            // container / deploy / test
            ContainerTestEnricher.class,
            ContainerTestExecuter.class,
            LocalTestExecuter.class,
            
            // execute the Test lifecycles, we want to execute Before after what we do
            BeforeLifecycleEventExecuter.class

            // core
      );
   }
}
