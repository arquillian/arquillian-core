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

import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * DeploymentWrapper
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentWrapper implements Deployment
{
   private DeploymentDescription deployment;
   
   public DeploymentWrapper(DeploymentDescription deployment)
   {
      this.deployment = deployment;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.deployment.Deployment#getName()
    */
   @Override
   public String getName()
   {
      return deployment.isArchiveDeployment() ? deployment.getTestableArchive().getName():deployment.getName();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.deployment.Deployment#getArchive()
    */
   @Override
   public Archive<?> getArchive()
   {
      return deployment.getTestableArchive() != null ? deployment.getTestableArchive():deployment.getArchive();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.deployment.Deployment#getDescriptor()
    */
   @Override
   public Descriptor getDescriptor()
   {
      return deployment.getDescriptor();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.deployment.Deployment#isDescriptorDeployment()
    */
   @Override
   public boolean isDescriptorDeployment()
   {
      return deployment.isDescriptorDeployment();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.deployment.Deployment#isArchiveDeployment()
    */
   @Override
   public boolean isArchiveDeployment()
   {
      return deployment.isArchiveDeployment();
   }
}
