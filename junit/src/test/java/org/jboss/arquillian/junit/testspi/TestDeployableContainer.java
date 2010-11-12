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
package org.jboss.arquillian.junit.testspi;

import org.jboss.arquillian.junit.JUnitIntegrationTestCase;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * TestDeployableContainer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestDeployableContainer implements DeployableContainer<TestContainerConfiguration>
{
   private int numberOfTimesDeployed = 0;
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getDefaultProtocol()
    */
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Local");
   }
   
   public Class<TestContainerConfiguration> getConfigurationClass()
   {
      return TestContainerConfiguration.class;
   }
   
   public void setup(TestContainerConfiguration configuration)
   {
      JUnitIntegrationTestCase.wasCalled("setup");
   }

   public void start() throws LifecycleException
   {
      JUnitIntegrationTestCase.wasCalled("start");
   }

   public void stop() throws LifecycleException
   {
      JUnitIntegrationTestCase.wasCalled("stop");
   }

   // TODO : implement tests
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
   }
   
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
   }

   public ProtocolMetaData deploy(Archive<?> deployment) throws DeploymentException
   {
      numberOfTimesDeployed++;
      JUnitIntegrationTestCase.wasCalled("deploy");
      if(numberOfTimesDeployed == 1) 
      {
         throw new RuntimeException("deploy");
      }

      return new ProtocolMetaData();
   }

   public void undeploy(Archive<?> deployment) throws DeploymentException
   {
      JUnitIntegrationTestCase.wasCalled("undeploy");
      if(numberOfTimesDeployed == 1)
      {
         throw new RuntimeException("undeploy");
      }
   }
}
