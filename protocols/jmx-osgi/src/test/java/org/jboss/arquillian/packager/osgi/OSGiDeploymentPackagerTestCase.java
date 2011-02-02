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
package org.jboss.arquillian.packager.osgi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;

import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.arquillian.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

/**
 * OSGiDeploymentPackagerTestCase
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiDeploymentPackagerTestCase
{
   @Test
   public void testValidBundle() throws Exception
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleManifestVersion(2);
            return builder.openStream();
         }
      });
      
      Archive<?> result = new OSGiDeploymentPackager().generateDeployment(new TestDeployment(archive, new ArrayList<Archive<?>>()), new ArrayList<ProtocolArchiveProcessor>());
      assertNotNull("Result archive not null", result);
   }
   
   @Test
   public void testInvalidBundle() throws Exception
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            return builder.openStream();
         }
      });
      try
      {
         new OSGiDeploymentPackager().generateDeployment(new TestDeployment(archive, new ArrayList<Archive<?>>()), new ArrayList<ProtocolArchiveProcessor>());
         fail("RuntimeException expected");
      }
      catch (RuntimeException ex)
      {
         // expected
      }
   }
}
