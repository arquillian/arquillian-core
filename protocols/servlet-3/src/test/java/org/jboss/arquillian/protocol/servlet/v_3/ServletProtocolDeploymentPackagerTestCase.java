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
package org.jboss.arquillian.protocol.servlet.v_3;

import java.util.ArrayList;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.protocol.servlet.v_3.ServletProtocolDeploymentPackager;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.ArchiveAsset;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * ServletProtocolDeploymentPackagerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletProtocolDeploymentPackagerTestCase
{
   @Test
   public void shouldHandleJavaArchive() throws Exception
   {
      Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                  ShrinkWrap.create(JavaArchive.class, "applicationArchive.jar"), 
                  createAuxiliaryArchives()));
      
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

   @Test
   public void shouldHandleWebArchive() throws Exception
   {
      Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                  ShrinkWrap.create(WebArchive.class, "applicationArchive.war"), 
                  createAuxiliaryArchives()));
      
      Assert.assertTrue(
            "Verify that a defined WebArchive using EE6 JavaArchive protocol is build as WebArchive",
            WebArchive.class.isInstance(archive));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/arquillian-protocol.jar")));
      
      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/auxiliaryArchive1.jar")));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/auxiliaryArchive2.jar")));
   }

   @Test
   public void shouldHandleEnterpriseArchive() throws Exception
   {
      Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                  ShrinkWrap.create(EnterpriseArchive.class, "applicationArchive.ear"), 
                  createAuxiliaryArchives()));
      
      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /",
            archive.contains(ArchivePaths.create("test.war")));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive1.jar")));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));
   }

   @Test
   @Ignore // TODO: Does not merge with existing archive
   public void shouldHandleEnterpriseArchiveWithExistingWAR() throws Exception
   {
      Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                  ShrinkWrap.create(EnterpriseArchive.class, "applicationArchive.ear")
                            .addModule(
                                  ShrinkWrap.create(WebArchive.class, "test.war")
                                            .addClass(Test.class)), 
                  createAuxiliaryArchives()));
      
      Assert.assertTrue(
            "Verify that the applicationArchive still contains WebArchive in /",
            archive.contains(ArchivePaths.create("test.war")));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));

      Archive<?> applicationArchive = ((ArchiveAsset)archive.get(ArchivePaths.create("test.war")).getAsset()).getArchive(); 
      Assert.assertTrue(
            "Verify that the auxiliaryArchive protocol is placed in applicationArchive WebArchive",
            applicationArchive.contains(ArchivePaths.create("/WEB-INF/lib/arquillian-protocol.jar")));

      Assert.assertTrue(
            "Verify that the applicationArchive has not been overwritten",
            applicationArchive.contains(ArchivePaths.create("/WEB-INF/classes/org/junit/Test.class")));

      
   }

   private Collection<Archive<?>> createAuxiliaryArchives() 
   {
      List<Archive<?>> archives = new ArrayList<Archive<?>>();
      archives.add(ShrinkWrap.create(JavaArchive.class, "auxiliaryArchive1.jar"));
      archives.add(ShrinkWrap.create(JavaArchive.class, "auxiliaryArchive2.jar"));
      
      return archives;
   }
}
