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
package org.jboss.arquillian.protocol.servlet.v_2_5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.protocol.servlet.TestUtil;
import org.jboss.arquillian.protocol.servlet.runner.ServletTestRunner;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
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
            "Verify that a defined JavaArchive using EE5 WebArchive protocol is build as EnterpriseArchive",
            EnterpriseArchive.class.isInstance(archive));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives EE Modules are placed in /",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive1.jar")));
      
      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));

      Assert.assertTrue(
            "Verify that the applicationArchive is placed in /",
            archive.contains(ArchivePaths.create("/applicationArchive.jar")));
   }
   

   @Test
   public void shouldHandleWebArchive() throws Exception
   {
      Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                  ShrinkWrap.create(WebArchive.class, "applicationArchive.war")
                     .addClass(getClass())
                     .setWebXML("test-web.xml"), 
                  createAuxiliaryArchives()));

      Assert.assertTrue(
            "verify that the ServletTestRunner was added to the archive",
            archive.contains("/WEB-INF/classes/org/jboss/arquillian/protocol/servlet/runner/ServletTestRunner.class"));


      String webXmlContent = TestUtil.convertToString(archive.get("WEB-INF/web.xml").getAsset().openStream());
      Assert.assertTrue(
            "verify that the ServletTestRunner servlet was added to the web.xml",
            webXmlContent.contains(ServletTestRunner.class.getName()));
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
            archive.contains(ArchivePaths.create("arquillian-protocol.war")));
      
      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive1.jar")));

      Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));
   }

   private Collection<Archive<?>> createAuxiliaryArchives() 
   {
      List<Archive<?>> archives = new ArrayList<Archive<?>>();
      archives.add(ShrinkWrap.create(JavaArchive.class, "auxiliaryArchive1.jar"));
      archives.add(ShrinkWrap.create(JavaArchive.class, "auxiliaryArchive2.jar"));
      
      return archives;
   }
}
