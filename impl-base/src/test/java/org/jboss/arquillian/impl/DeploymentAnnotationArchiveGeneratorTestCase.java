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

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.junit.Test;

/**
 * DeploymentAnnotationArchiveGeneratorTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentAnnotationArchiveGeneratorTestCase
{

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentNotPresent() throws Exception
   {
      new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(new TestClass(DeploymentNotPresent.class));
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentNotStatic() throws Exception
   {
      new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(new TestClass(DeploymentNotStatic.class));
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnDeploymentWrongReturnType() throws Exception
   {
      new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(new TestClass(DeploymentWrongReturnType.class));
   }

   @Test
   public void shouldThrowExceptionOnDeploymentOk() throws Exception
   {
      Archive<?> archive = new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(new TestClass(DeploymentOK.class));

      ArchivePath testPath = ArchivePaths.create(DeploymentOK.class.getName().replaceAll("\\.", "/") + ".class");

      // verify that the test class was added to the archive
      Assert.assertNotNull(archive.contains(testPath));
   }

   @Test
   public void shouldNotIncludeTheTestClassIfClassesNotSupportedByTheArchive() throws Exception
   {
      Archive<?> archive = new DeploymentAnnotationArchiveGenerator().generateApplicationArchive(new TestClass(DeploymentClassesNotSupported.class));

      // verify that nothing was added to the archive
      Assert.assertTrue(archive.getContent().isEmpty());
   }

   private static class DeploymentNotPresent
   {
   }

   private static class DeploymentNotStatic
   {
      @SuppressWarnings("unused")
      @Deployment
      public Archive<?> test()
      {
         return ShrinkWrap.create(JavaArchive.class, "test.jar");
      }
   }

   private static class DeploymentWrongReturnType
   {
      @SuppressWarnings("unused")
      @Deployment
      public Object test()
      {
         return ShrinkWrap.create(JavaArchive.class, "test.jar");
      }
   }

   private static class DeploymentOK
   {
      @SuppressWarnings("unused")
      @Deployment
      public static JavaArchive test()
      {
         return ShrinkWrap.create(JavaArchive.class, "test.jar");
      }
   }

   private static class DeploymentClassesNotSupported
   {
      @SuppressWarnings("unused")
      @Deployment
      public static ResourceAdapterArchive test()
      {
         return ShrinkWrap.create(ResourceAdapterArchive.class, "test.jar");
      }
   }
}
