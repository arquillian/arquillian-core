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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AnnotationDeploymentScenarioGeneratorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class AnnotationDeploymentScenarioGeneratorTestCase
{

   private static Logger log = Logger.getLogger(AnnotationDeploymentScenarioGenerator.class.getName());
   private static OutputStream logCapturingStream;
   private static StreamHandler customLogHandler;
   private final static String expectedLogPartForArchiveWithUnexpectedFileExtension = "unexpected file extension";

   @Before
   public void attachLogCapturer()
   {
      logCapturingStream = new ByteArrayOutputStream();
      Handler[] handlers = log.getParent().getHandlers();
      customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
      log.addHandler(customLogHandler);
   }

   @After
   public void detachLagCapturer()
   {
      log.removeHandler(customLogHandler);
      customLogHandler = null;
      try
      {
         logCapturingStream.close();
      } catch (IOException e)
      {
         throw new IllegalStateException("Potential memory leak as log capturing stream could not be closed");
      }
      logCapturingStream = null;
   }

   public String getTestCapturedLog() throws IOException
   {
      customLogHandler.flush();
      return logCapturingStream.toString();
   }

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
         Assert.assertTrue(Validate.isArchiveOfType(JavaArchive.class, deployment.getArchive()));
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
      Assert.assertTrue(Validate.isArchiveOfType(JavaArchive.class, deploymentOne.getArchive()));
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
      Assert.assertTrue(Validate.isArchiveOfType(JavaArchive.class, deploymentTwo.getArchive()));
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
      Assert.assertTrue(Validate.isArchiveOfType(JavaArchive.class, deploymentOne.getArchive()));
      Assert.assertEquals(Exception.class, deploymentOne.getExpectedException());
   }

   @Test
   public void shouldAllowNoDeploymentPresent() throws Exception
   {
      List<DeploymentDescription> descriptors = generate(DeploymentNotPresent.class);

      Assert.assertNotNull(descriptors);
      Assert.assertEquals(0, descriptors.size());
   }

   @Test
   public void shouldAllowNonPublicDeploymentMethods() throws Exception {
      List<DeploymentDescription> descriptors = generate(DeploymentProtectedMethods.class);

      Assert.assertNotNull(descriptors);
      Assert.assertEquals(3, descriptors.size());
   }

   @Test
   public void shouldAllowNonPublicDeploymentMethodsFromSuperClass() throws Exception {
      List<DeploymentDescription> descriptors = generate(DeploymentProtectedMethodsInherited.class);

      Assert.assertNotNull(descriptors);
      Assert.assertEquals(3, descriptors.size());
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

   @Test
   public void shouldLogWarningForMismatchingArchiveTypeAndFileExtension() throws Exception
   {
      new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentWithMismatchingTypeAndFileExtension.class));

      String capturedLog = getTestCapturedLog();
      Assert.assertTrue(capturedLog.contains(expectedLogPartForArchiveWithUnexpectedFileExtension));
   }

   @Test
   public void shouldNotLogWarningForMatchingArchiveTypeAndFileExtension() throws Exception
   {
      new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentWithSpecifiedFileExtension.class));

      String capturedLog = getTestCapturedLog();
      Assert.assertFalse(capturedLog.contains(expectedLogPartForArchiveWithUnexpectedFileExtension));
   }

   @Test
   public void shouldLogWarningForDeploymentWithMissingFileExtension() throws Exception
   {
      new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentWithMissingFileExtension.class));

      String capturedLog = getTestCapturedLog();
      Assert.assertTrue(capturedLog.contains(expectedLogPartForArchiveWithUnexpectedFileExtension));
   }

   @Test // should not log warning when using the default archive name
   public void shouldNotLogWarningForDeploymentWithoutSpecifiedName() throws Exception
   {
      new AnnotationDeploymentScenarioGenerator().generate(
            new TestClass(DeploymentWithoutSpecifiedName.class));

      String capturedLog = getTestCapturedLog();
      Assert.assertFalse(capturedLog.contains(expectedLogPartForArchiveWithUnexpectedFileExtension));
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
   private static class ExpectedDeploymentExceptionSet
   {
      @Deployment(name = "second", testable = true) // testable should be overwritten by @Expected
      @ShouldThrowException
      public static Archive<?> deploymentOne()
      {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }

   @SuppressWarnings("unused")
   private static class DeploymentProtectedMethods {

      @Deployment
      static JavaArchive one() {
         return ShrinkWrap.create(JavaArchive.class);
      }

      @Deployment
      private static JavaArchive two() {
         return ShrinkWrap.create(JavaArchive.class);
      }

      @Deployment
      protected static JavaArchive tree() {
         return ShrinkWrap.create(JavaArchive.class);
      }
   }

   private static class DeploymentProtectedMethodsInherited extends DeploymentProtectedMethods {
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

   @SuppressWarnings("unused")
   private static class DeploymentWithMismatchingTypeAndFileExtension
   {
      @Deployment
      public static WebArchive test()
      {
         return ShrinkWrap.create(WebArchive.class, "test.jar");
      }
   }

   @SuppressWarnings("unused")
   private static class DeploymentWithSpecifiedFileExtension
   {
      @Deployment
      public static WebArchive test()
      {
         return ShrinkWrap.create(WebArchive.class, "test.war");
      }
   }

   @SuppressWarnings("unused")
   private static class DeploymentWithMissingFileExtension
   {
      @Deployment
      public static WebArchive test()
      {
         return ShrinkWrap.create(WebArchive.class, "test");
      }
   }

   @SuppressWarnings("unused")
   private static class DeploymentWithoutSpecifiedName
   {
      @Deployment
      public static WebArchive test()
      {
         return ShrinkWrap.create(WebArchive.class);
      }
   }

   private List<DeploymentDescription> generate(Class<?> testClass)
   {
      return new AnnotationDeploymentScenarioGenerator().generate(new TestClass(testClass));
   }
}

