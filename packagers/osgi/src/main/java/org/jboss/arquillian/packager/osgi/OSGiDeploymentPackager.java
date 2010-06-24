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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;

import org.jboss.arquillian.spi.DeploymentPackager;
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
   public Archive<?> generateDeployment(Archive<?> bundleArchive, Collection<Archive<?>> auxiliaryArchives)
   {
      if(JavaArchive.class.isInstance(bundleArchive))
      {
         return handleArchive(JavaArchive.class.cast(bundleArchive), auxiliaryArchives);
      }
      
      throw new IllegalArgumentException(OSGiDeploymentPackager.class.getName()  + 
            " can not handle archive of type " +  bundleArchive.getClass().getName());
   }

   private Archive<?> handleArchive(JavaArchive archive, Collection<Archive<?>> auxiliaryArchives) 
   {
      try
      {
         VirtualFile virtualFile = toVirtualFile(archive);
         BundleInfo info = BundleInfo.createBundleInfo(virtualFile);
         return new BundleArchive(archive, info);
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

   private VirtualFile toVirtualFile(Archive<?> archive) throws IOException, MalformedURLException
   {
      // [TODO] Can this be done in memory?
      ZipExporter exporter = archive.as(ZipExporter.class);
      String archiveName = archive.getName();
      int dotIndex = archiveName.lastIndexOf(".");
      if (dotIndex > 0)
         archiveName = archiveName.substring(0, dotIndex);
      File target = File.createTempFile(archiveName + "-", ".jar");
      exporter.exportZip(target, true);
      target.deleteOnExit();
      return AbstractVFS.getRoot(target.toURI().toURL());
   }
}