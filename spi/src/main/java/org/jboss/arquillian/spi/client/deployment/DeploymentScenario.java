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
package org.jboss.arquillian.spi.client.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.test.DeploymentTargetDescription;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.jboss.arquillian.spi.util.Validate;

/**
 *  
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentScenario
{
   private List<DeploymentDescription> deployments;

   private RunModeType runMode;
   
   public DeploymentScenario(RunModeType runMode)
   {
      Validate.notNull(runMode, "RunMode must be specified");
      this.deployments = new ArrayList<DeploymentDescription>();
      this.runMode = runMode;
   }
   
   /**
    * 
    * @return the runMode
    */
   public RunModeType getRunMode()
   {
      return runMode;
   }
   
   public DeploymentScenario addDeployment(DeploymentDescription deployment)
   {
      Validate.notNull(deployment, "Deployment must be specified");
      this.deployments.add(deployment);
      return this;
   }
   
   public Set<TargetDescription> getTargets()
   {
      Set<TargetDescription> targets = new HashSet<TargetDescription>();
      
      for(DeploymentDescription desc : deployments)
      {
         targets.add(desc.getTarget());
      }
      return targets;
   }
   
   public Set<ProtocolDescription> getProtocols()
   {
      Set<ProtocolDescription> protocols = new HashSet<ProtocolDescription>();
      
      for(DeploymentDescription desc : deployments)
      {
         protocols.add(desc.getProtocol());
      }
      return protocols;
   }

   /** 
    * Get a {@link DeploymentDescription} with a specific name if it exists.
    * 
    * @param target The name of the {@link DeploymentDescription}
    * @return Defined Deployment or null if not found.
    */
   public DeploymentDescription getDeployment(DeploymentTargetDescription target)
   {
      Validate.notNull(target, "Target must be specified");
      if(DeploymentTargetDescription.DEFAULT.equals(target))
      {
         return findDefaultDeployment();
      }
      return findMatchingDeployment(target);
   }
   
   /**
    * @return
    */
   private DeploymentDescription findDefaultDeployment()
   {
      if(deployments.size() == 1)
      {
         return deployments.get(0);
      }
      return null;
   }

   /**
    * @param target
    * @return
    */
   private DeploymentDescription findMatchingDeployment(DeploymentTargetDescription target)
   {
      for(DeploymentDescription deployment : deployments)
      {
         if(deployment.getName().equals(target.getName()))
         {
            return deployment;
         }
      }
      return null;
   }

   /**
    * Get all {@link DeploymentDescription} defined to be deployed during Test startup for a specific {@link TargetDescription} ordered.
    * 
    * @param target The Target to filter on
    * @return A List of found {@link DeploymentDescription}. Will return a empty list if none are found.
    */
   public List<DeploymentDescription> getStartupDeploymentsFor(TargetDescription target)
   {
      Validate.notNull(target, "Target must be specified");
      List<DeploymentDescription> startupDeployments = new ArrayList<DeploymentDescription>();
      for(DeploymentDescription deployment : deployments)
      {
         if(deployment.deployOnStartup() && target.equals(deployment.getTarget()))
         {
            startupDeployments.add(deployment);
         }
      }
      // sort them by order
      Collections.sort(startupDeployments, new Comparator<DeploymentDescription>()
      {
         public int compare(DeploymentDescription o1, DeploymentDescription o2)
         {
            return new Integer(o1.getOrder()).compareTo(o2.getOrder());
         }
      });
      
      return Collections.unmodifiableList(startupDeployments);
   }
   
   /**
    * @return the deployments
    */
   public List<DeploymentDescription> getDeployments()
   {
      return Collections.unmodifiableList(deployments);
   }
}
