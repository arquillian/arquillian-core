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
package org.jboss.arquillian.spi.event.container;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.util.Validate;

/**
 * DeployerEvent
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeployerEvent extends ContainerEvent
{
   private Deployment[] deployments;
   
   public DeployerEvent(DeployableContainer<?> deployableContainer, Deployment... deployments)
   {
      super(deployableContainer);
      Validate.notNull(deployments, "Deployments must be specified");
      this.deployments = deployments;
   }
   
   /**
    * @return the deployments
    */
   public Deployment[] getDeployments()
   {
      return deployments;
   }
}
