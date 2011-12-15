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
package org.jboss.arquillian.spi.client.deployment;

import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentTargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.junit.Assert;
import org.junit.Test;

/**
 * DeploymentScenarioTest
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentScenarioTestCase
{
   private final static String DEFAULT_NAME = DeploymentTargetDescription.DEFAULT.getName();

   /**
    * Defaulting rules for Deployment in a scenario
    * 
    * - A single Archive is default
    * - A Archive and a Descriptor, Archive is default
    * - Only allow equal names of deployments if they are of different Types
    * 
    */

   @Test
   public void shouldDefaultToSingleArchive()
   {
      DeploymentDescription deployment = new DeploymentDescription(DEFAULT_NAME, ShrinkWrap.create(JavaArchive.class));
      deployment.setTarget(TargetDescription.DEFAULT);
      
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(deployment);
      
      DeploymentDescription defaultDeployment = scenario.deployment(DeploymentTargetDescription.DEFAULT).getDescription();
      
      Assert.assertEquals(deployment, defaultDeployment);
   }

   @Test
   public void shouldDefaultToSingleDescriptor()
   {
      DeploymentDescription deployment = new DeploymentDescription(DEFAULT_NAME, Descriptors.create(BeansDescriptor.class));
      deployment.setTarget(TargetDescription.DEFAULT);
      
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(deployment);
      
      DeploymentDescription defaultDeployment = scenario.deployment(DeploymentTargetDescription.DEFAULT).getDescription();
      
      Assert.assertEquals(deployment, defaultDeployment);
   }

   @Test
   public void shouldDefaultToArchiveWhenDescriptorIsPresent()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription("A", ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription("B", Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));


      DeploymentDescription defaultDeployment = scenario.deployment(DeploymentTargetDescription.DEFAULT).getDescription();
      
      Assert.assertEquals("A", defaultDeployment.getName());
   }

   @Test
   public void shouldNotDefaultWhenMultipleArchives()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription("A", ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription("B", ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
      
      Deployment defaultDeployment = scenario.deployment(DeploymentTargetDescription.DEFAULT);
      
      Assert.assertNull(defaultDeployment);
   }

   @Test
   public void shouldDefaultToDefaultWithMultipleDeployments()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription("A", ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription(DEFAULT_NAME, Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription(DEFAULT_NAME, ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));

      Deployment defaultDeployment = scenario.deployment(DeploymentTargetDescription.DEFAULT);
      Assert.assertNotNull(defaultDeployment);
      Assert.assertEquals(DEFAULT_NAME, defaultDeployment.getDescription().getName());
      Assert.assertTrue(defaultDeployment.getDescription().isArchiveDeployment());
   }

   @Test
   public void shouldNotDefaultWhenMultipleDescriptors()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription("A", Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription("B", Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));
      
      Deployment defaultDeployment = scenario.deployment(DeploymentTargetDescription.DEFAULT);
      
      Assert.assertNull(defaultDeployment);
   }

   @Test
   public void shouldNotGetDefaultWithNonDefaultName()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription("A", ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription("B", Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));


      DeploymentDescription deployment = scenario.deployment(new DeploymentTargetDescription("B")).getDescription();
      
      Assert.assertEquals("B", deployment.getName());
   }

   @Test
   public void shouldNotGetWithUnknownName()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription("A", ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription("B", Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));


      Deployment deployment = scenario.deployment(new DeploymentTargetDescription("C"));
      
      Assert.assertNull(deployment);
   }

   @Test
   public void shouldAllowMultipleDeploymentWithSameNameOfDifferentType()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription("A", ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription("A", Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));

      Deployment deployment = scenario.deployment(new DeploymentTargetDescription("A"));

      // will default to Archive
      Assert.assertEquals("A", deployment.getDescription().getName());
      Assert.assertTrue(deployment.getDescription().isArchiveDeployment());
   }

   @Test // checks same as shouldAllowMultipleDeploymentWithSameNameOfDifferentType but added in different order
   public void shouldAllowMultipleDeploymentWithSameNameOfDifferentTypeOrderIrrelevant()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription("A", Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription("A", ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));

      Deployment deployment = scenario.deployment(new DeploymentTargetDescription("A"));

      // will default to Archive
      Assert.assertEquals("A", deployment.getDescription().getName());
      Assert.assertTrue(deployment.getDescription().isArchiveDeployment());
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldNotAllowMultipleArchiveDeploymentsWithSameName()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription(DEFAULT_NAME, ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription(DEFAULT_NAME, ShrinkWrap.create(JavaArchive.class))
            .setTarget(TargetDescription.DEFAULT));
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldNotAllowMultipleDescriptorDeploymentsWithSameName()
   {
      DeploymentScenario scenario = new DeploymentScenario();
      scenario.addDeployment(
            new DeploymentDescription(DEFAULT_NAME, Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));
      scenario.addDeployment(
            new DeploymentDescription(DEFAULT_NAME, Descriptors.create(BeansDescriptor.class))
            .setTarget(TargetDescription.DEFAULT));
   }
}
