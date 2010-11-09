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
package org.jboss.arquillian.impl.client.deployment;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Protocol;
import org.jboss.arquillian.api.Target;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;


/**
 * AnnotationDeploymentScenarioGeneratorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class AnnotationDeploymentScenarioGeneratorTestCase
{

   @Test
   public void shouldHandleMultipleDeploymentsAllDefault() throws Exception
   {
      DeploymentScenario scenario = new AnnotationDeploymentScenarioGenerator().generate(new TestClass(MultiDeploymentsDefault.class));
      
      Assert.assertNotNull(scenario);
      Assert.assertEquals(
            "Verify all deployments were found",
            2, scenario.getDeployments().size());
      
      for(DeploymentDescription deployment : scenario.getDeployments())
      {
         Assert.assertEquals(
               "Verify deployment has default target",
               TargetDescription.DEFAULT,
               deployment.getTarget());

         Assert.assertEquals(
               "Verify deployment has default protocol",
               ProtocolDescription.DEFAULT,
               deployment.getProtocol());
         
         Assert.assertEquals(-1, deployment.getOrder());
         Assert.assertEquals(true, deployment.deployOnStartup());
         Assert.assertTrue(JavaArchive.class.isInstance(deployment.getArchive()));
      }
   }
   
   @Test
   public void shouldHandleMultipleDeploymentsAllSet() throws Exception
   {
      DeploymentScenario scenario = new AnnotationDeploymentScenarioGenerator().generate(new TestClass(MultiDeploymentsSet.class));
      
      Assert.assertNotNull(scenario);
      Assert.assertEquals(
            "Verify all deployments were found",
            2, scenario.getDeployments().size());
      
      DeploymentDescription deploymentOne = scenario.getDeployments().get(0);

      Assert.assertEquals(
            "Verify deployment has specified target",
            new TargetDescription("target-first"),
            deploymentOne.getTarget());

      Assert.assertEquals(
            "Verify deployment has specified protocol",
            new ProtocolDescription("protocol-first"),
            deploymentOne.getProtocol());
      
      Assert.assertEquals(1, deploymentOne.getOrder());
      Assert.assertEquals(false, deploymentOne.deployOnStartup());
      Assert.assertEquals(false, deploymentOne.testable());
      Assert.assertTrue(JavaArchive.class.isInstance(deploymentOne.getArchive()));

      DeploymentDescription deploymentTwo = scenario.getDeployments().get(1);

      Assert.assertEquals(
            "Verify deployment has specified target",
            new TargetDescription("target-second"),
            deploymentTwo.getTarget());
      Assert.assertEquals(
            "Verify deployment has specified protocol",
            new ProtocolDescription("protocol-second"),
            deploymentTwo.getProtocol());
      
      Assert.assertEquals(2, deploymentTwo.getOrder());
      Assert.assertEquals(false, deploymentTwo.deployOnStartup());
      Assert.assertEquals(true, deploymentTwo.testable());
      Assert.assertTrue(JavaArchive.class.isInstance(deploymentTwo.getArchive()));

   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentNotPresent() throws Exception
   {
      new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentNotPresent.class));
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentNotStatic() throws Exception
   {
      new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentNotStatic.class));
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentWrongReturnType() throws Exception
   {
      new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentWrongReturnType.class));
   }

   @SuppressWarnings("unused")
   private static class MultiDeploymentsDefault 
   {
      @Deployment
      public static Archive<?> deploymentOne()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }

      @Deployment
      public static Archive<?> deploymentTwo()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }

   @SuppressWarnings("unused")
   private static class MultiDeploymentsSet 
   {
      @Protocol("protocol-first")
      @Target("target-first")
      @Deployment(name = "first", order = 1, startup = false, testable = false)
      public static Archive<?> deploymentOne()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }

      @Protocol("protocol-second")
      @Target("target-second")
      @Deployment(name = "second", order = 2, startup = false)
      public static Archive<?> deploymentTwo()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }
   
   private static class DeploymentNotPresent
   {
   }

   @SuppressWarnings("unused")
   private static class DeploymentNotStatic
   {
      @Deployment
      public Archive<?> test()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }

   @SuppressWarnings("unused")
   private static class DeploymentWrongReturnType
   {
      @Deployment
      public Object test()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }
}

