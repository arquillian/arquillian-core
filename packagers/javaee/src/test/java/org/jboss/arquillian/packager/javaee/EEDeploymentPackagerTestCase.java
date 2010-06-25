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
package org.jboss.arquillian.packager.javaee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.spi.Context;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * EEDeploymentPackagerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EEDeploymentPackagerTestCase
{
   @Test
   public void shouldHandleJavaArchiveEE5Protocol() throws Exception
   {
      Context context = Mockito.mock(Context.class);
      Archive<?> archive = new EEDeploymentPackager().generateDeployment(context,
            ShrinkWrap.create("applicationArchive.jar", JavaArchive.class), 
            createAuxiliaryArchivesEE5());
      
      Assert.assertTrue(
            "Verify that a defined JavaArchive using EE5 WebArchive protocol is build as EnterpriseArchive",
            EnterpriseArchive.class.isInstance(archive));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives EE Modules are placed in /",
            archive.contains(ArchivePaths.create("/arquillian-protocol.war")));
      
      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));

      Assert.assertTrue(
            "Verify that the applicationArchive is placed in /",
            archive.contains(ArchivePaths.create("/applicationArchive.jar")));
   }
   
   @Test
   public void shouldHandleJavaArchiveEE6Protocol() throws Exception
   {
      Context context = Mockito.mock(Context.class);
      Archive<?> archive = new EEDeploymentPackager().generateDeployment(context,
            ShrinkWrap.create("applicationArchive.jar", JavaArchive.class), 
            createAuxiliaryArchivesEE6());
      
      Assert.assertTrue(
            "Verify that a defined JavaArchive using EE6 JavaArchive protocol is build as WebArchive",
            WebArchive.class.isInstance(archive));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/arquillian-protocol.jar")));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/auxiliaryArchive2.jar")));
      
      Assert.assertTrue(
            "Verify that the applicationArchive is placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/applicationArchive.jar")));
   }

   // as of now, War inside War is not supported. need to merge ? 
   @Test(expected = IllegalArgumentException.class)
   public void shouldHandleWebArchiveEE5Protocol() throws Exception
   {
      Context context = Mockito.mock(Context.class);
      new EEDeploymentPackager().generateDeployment(context,
            ShrinkWrap.create("applicationArchive.war", WebArchive.class), 
            createAuxiliaryArchivesEE5());
      
   }

   @Test
   public void shouldHandleWebArchiveEE6Protocol() throws Exception
   {
      Context context = Mockito.mock(Context.class);
      Archive<?> archive = new EEDeploymentPackager().generateDeployment(context,
            ShrinkWrap.create("applicationArchive.war", WebArchive.class), 
            createAuxiliaryArchivesEE6());
      
      Assert.assertTrue(
            "Verify that a defined WebArchive using EE6 JavaArchive protocol is build as WebArchive",
            WebArchive.class.isInstance(archive));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/arquillian-protocol.jar")));
      
      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/auxiliaryArchive2.jar")));
   }

   @Test
   public void shouldHandleEnterpriseArchiveEE5Protocol() throws Exception
   {
      Context context = Mockito.mock(Context.class);
      Archive<?> archive = new EEDeploymentPackager().generateDeployment(context,
            ShrinkWrap.create("applicationArchive.ear", EnterpriseArchive.class), 
            createAuxiliaryArchivesEE5());

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /",
            archive.contains(ArchivePaths.create("arquillian-protocol.war")));
      
      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));

   }

   @Test
   public void shouldHandleEnterpriseArchiveEE6Protocol() throws Exception
   {
      Context context = Mockito.mock(Context.class);
      Archive<?> archive = new EEDeploymentPackager().generateDeployment(context,
            ShrinkWrap.create("applicationArchive.ear", EnterpriseArchive.class), 
            createAuxiliaryArchivesEE6());
      
      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /",
            archive.contains(ArchivePaths.create("test.war")));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));
   }

   private Collection<Archive<?>> createAuxiliaryArchivesEE6() 
   {
      List<Archive<?>> archives = new ArrayList<Archive<?>>();
      archives.add(ShrinkWrap.create("arquillian-protocol.jar", JavaArchive.class));
      archives.add(ShrinkWrap.create("auxiliaryArchive2.jar", JavaArchive.class));
      
      return archives;
   }

   private Collection<Archive<?>> createAuxiliaryArchivesEE5() 
   {
      List<Archive<?>> archives = new ArrayList<Archive<?>>();
      archives.add(ShrinkWrap.create("arquillian-protocol.war", WebArchive.class));
      archives.add(ShrinkWrap.create("auxiliaryArchive2.jar", JavaArchive.class));
      
      return archives;
   }
}
