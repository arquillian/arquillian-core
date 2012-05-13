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
package org.jboss.arquillian.container.test.impl.client.deployment;

import java.util.List;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ExcludeServices;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ServiceType;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
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
      List<DeploymentDescription> scenario = generate(MultiDeploymentsDefault.class);
      
      Assert.assertNotNull(scenario);
      Assert.assertEquals(
            "Verify all deployments were found",
            2, scenario.size());
      
      for(DeploymentDescription deployment : scenario)
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
         Assert.assertEquals(true, deployment.managed());
         Assert.assertTrue(JavaArchive.class.isInstance(deployment.getArchive()));
      }
   }
   
   @Test
   public void shouldHandleMultipleDeploymentsAllSet() throws Exception
   {
      List<DeploymentDescription> scenario = generate(MultiDeploymentsSet.class);
      
      Assert.assertNotNull(scenario);
      Assert.assertEquals(
            "Verify all deployments were found",
            2, scenario.size());
      
      DeploymentDescription deploymentOne = scenario.get(0);

      Assert.assertEquals(
            "Verify deployment has specified target",
            new TargetDescription("target-first"),
            deploymentOne.getTarget());

      Assert.assertEquals(
            "Verify deployment has specified protocol",
            new ProtocolDescription("protocol-first"),
            deploymentOne.getProtocol());
      
      Assert.assertEquals(1, deploymentOne.getOrder());
      Assert.assertEquals(false, deploymentOne.managed());
      Assert.assertEquals(false, deploymentOne.testable());
      Assert.assertTrue(JavaArchive.class.isInstance(deploymentOne.getArchive()));
      Assert.assertNull(deploymentOne.getExpectedException());
      
      DeploymentDescription deploymentTwo = scenario.get(1);

      Assert.assertEquals(
            "Verify deployment has specified target",
            new TargetDescription("target-second"),
            deploymentTwo.getTarget());
      Assert.assertEquals(
            "Verify deployment has specified protocol",
            new ProtocolDescription("protocol-second"),
            deploymentTwo.getProtocol());
      
      Assert.assertEquals(2, deploymentTwo.getOrder());
      Assert.assertEquals(false, deploymentTwo.managed());
      Assert.assertEquals(true, deploymentTwo.testable());
      Assert.assertTrue(JavaArchive.class.isInstance(deploymentTwo.getArchive()));
      Assert.assertNull(deploymentTwo.getExpectedException());
   }
   
   @Test
   public void shouldReadExpectedAndOverrideDeployment()
   {
      List<DeploymentDescription> scenario = generate(ExpectedDeploymentExceptionSet.class);
      
      Assert.assertNotNull(scenario);
      Assert.assertEquals(
            "Verify all deployments were found",
            1, scenario.size());
      
      DeploymentDescription deploymentOne = scenario.get(0);

      Assert.assertEquals(false, deploymentOne.testable());
      Assert.assertTrue(JavaArchive.class.isInstance(deploymentOne.getArchive()));
      Assert.assertEquals(Exception.class, deploymentOne.getExpectedException());
   }
   
   @Test
   public void shouldThrowExceptionOnFieldOverridesTestableDeployment()
   {
      List<DeploymentDescription> scenario = generate(ShouldThrowExceptionOnDeploymentField.class);
      
      Assert.assertNotNull(scenario);
      Assert.assertEquals(
            "Verify all deployments were found",
            1, scenario.size());
      
      DeploymentDescription deploymentOne = scenario.get(0);

      Assert.assertEquals(false, deploymentOne.testable());
      Assert.assertTrue(JavaArchive.class.isInstance(deploymentOne.getArchive()));
      Assert.assertEquals(Exception.class, deploymentOne.getExpectedException());
   }

   @Test
   public void shouldAcceptDeploymentWithExcludedServices()
   {
       List<DeploymentDescription> descriptors = new AnnotationDeploymentScenarioGenerator().generate(
               new TestClass(DeploymentWithExcludedServices.class));
       Assert.assertNotNull(descriptors);
       Assert.assertEquals(1, descriptors.size());
       DeploymentDescription deployment = descriptors.get(0);
       Assert.assertFalse(deployment.testable());
       Assert.assertEquals(1, deployment.servicesToExclude().size());
       Assert.assertEquals(ServiceType.ALL, deployment.servicesToExclude().get(0));
   }
   
   @Test
   public void shouldAcceptDeploymentWithTestRunnerServiceExcluded()
   {
       List<DeploymentDescription> descriptors = new AnnotationDeploymentScenarioGenerator().generate(
               new TestClass(DeploymentWithTestRunnerServiceExcluded.class));
       Assert.assertNotNull(descriptors);
       Assert.assertEquals(1, descriptors.size());
       DeploymentDescription deployment = descriptors.get(0);
       Assert.assertFalse(deployment.testable());
       Assert.assertEquals(1, deployment.servicesToExclude().size());
       Assert.assertEquals(ServiceType.TEST_RUNNER, deployment.servicesToExclude().get(0));
   }
   
   @Test
   public void shouldAllowDeploymentOnField() throws Exception
   {
      List<DeploymentDescription> descriptors = new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentOnField.class));
      
      Assert.assertNotNull(descriptors);
      Assert.assertEquals(1, descriptors.size());
      Assert.assertTrue(descriptors.get(0).testable());
   }
   
   @Test
   public void shouldAllowNoDeploymentPresent() throws Exception
   {
      List<DeploymentDescription> descriptors = new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentNotPresent.class));
      
      Assert.assertNotNull(descriptors);
      Assert.assertEquals(0, descriptors.size());
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
      @OverProtocol("protocol-first")
      @TargetsContainer("target-first")
      @Deployment(name = "first", order = 1, managed = false, testable = false)
      public static Archive<?> deploymentOne()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }

      @OverProtocol("protocol-second")
      @TargetsContainer("target-second")
      @Deployment(name = "second", order = 2, managed = false)
      public static Archive<?> deploymentTwo()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }

   @SuppressWarnings("unused")
   private static class DeploymentWithExcludedServices 
   {
      @Deployment
      @ExcludeServices
      public static Archive<?> createDeployment()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }
   
   @SuppressWarnings("unused")
   private static class DeploymentWithTestRunnerServiceExcluded
   {
      @Deployment
      @ExcludeServices(ServiceType.TEST_RUNNER)
      public static Archive<?> createDeployment()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }
   
   @SuppressWarnings("unused")
   private static class ExpectedDeploymentExceptionSet 
   {
      @Deployment(name = "second", testable = true) // testable should be overwritten by @Expected
      @ShouldThrowException(Exception.class)
      public static Archive<?> deploymentOne()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }
   
   @SuppressWarnings("unused")
   private static class DeploymentOnField
   {
      @Deployment
      @ExcludeServices({})
      public static Archive<?> deployment = ShrinkWrap.create(JavaArchive.class);
   }
   
   @SuppressWarnings("unused")
   private static class ShouldThrowExceptionOnDeploymentField 
   {
      @Deployment(testable = true) // testable should be overwritten by @ShouldThrowException
      @ShouldThrowException(Exception.class)
      public static Archive<?> deployment = ShrinkWrap.create(JavaArchive.class);
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
   
   private List<DeploymentDescription> generate(Class<?> testClass)
   {
      return new AnnotationDeploymentScenarioGenerator().generate(new TestClass(testClass));
   }
}

