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

import java.io.File;
import java.util.Collection;

import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentPackager;
import org.jboss.osgi.spi.util.BundleInfo;
import org.jboss.osgi.vfs.AbstractVFS;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Packager for running Arquillian against OSGi containers.
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiDeploymentPackager implements DeploymentPackager
{
   public Archive<?> generateDeployment(TestDeployment testDeployment)
   {
      Archive<?> bundleArchive = testDeployment.getApplicationArchive();
      if(JavaArchive.class.isInstance(bundleArchive))
      {
         return handleArchive(JavaArchive.class.cast(bundleArchive), testDeployment.getAuxiliaryArchives());
      }
      
      throw new IllegalArgumentException(OSGiDeploymentPackager.class.getName()  + 
            " can not handle archive of type " +  bundleArchive.getClass().getName());
   }

   private Archive<?> handleArchive(JavaArchive archive, Collection<Archive<?>> auxiliaryArchives) 
   {
      try
      {
         validateBundleArchive(archive);
         return archive;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new IllegalArgumentException("Not a valid OSGi bundle: " + archive, ex);
      }
   }

   private void validateBundleArchive(Archive<?> archive) throws Exception
   {
      String archiveName = archive.getName();
      int dotIndex = archiveName.lastIndexOf(".");
      if (dotIndex > 0)
         archiveName = archiveName.substring(0, dotIndex);
      
      // [TODO] Can this be done in memory?
      File target = File.createTempFile(archiveName + "-", ".jar");
      try
      {
         ZipExporter exporter = archive.as(ZipExporter.class);
         exporter.exportZip(target, true);
         VirtualFile virtualFile = AbstractVFS.getRoot(target.toURI().toURL());
         BundleInfo.createBundleInfo(virtualFile);
      }
      finally
      {
         target.delete();
      }
   }
}
