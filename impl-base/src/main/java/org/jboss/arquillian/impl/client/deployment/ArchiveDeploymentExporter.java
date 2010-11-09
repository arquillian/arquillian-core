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
package org.jboss.arquillian.impl.client.deployment;

import java.io.File;
import java.util.logging.Logger;

import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * Handler that will export the generated {@link Archive} to the file system. <br/>
 * Used for debugging the deployment.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
// TODO: implement
public class ArchiveDeploymentExporter 
{
//   private static final Logger log = Logger.getLogger(ArchiveDeploymentExporter.class.getName());

   public void callback(BeforeDeploy event) throws Exception
   {
//      Archive<?> deployment = context.get(Archive.class);
//      Configuration configuration = context.get(Configuration.class);
//
//      if (deployment != null && configuration != null && configuration.getDeploymentExportPath() != null)
//      {
//         // TODO: should prepping the export directory be in the configuration builder?
//         String exportPath = configuration.getDeploymentExportPath();
//         File exportDir = new File(exportPath);
//         if (exportDir.isFile())
//         {
//            log.warning("Deployment export disabled. Export path points to an existing file: " + exportPath);
//            return;
//         }
//         else if (!exportDir.isDirectory() && !exportDir.mkdirs())
//         {
//            log.warning("Deployment export directory could not be created: " + exportPath);
//            return;
//         }
//
//         deployment.as(ZipExporter.class).exportZip(
//            new File(exportDir, event.getTestClass().getName() + "_" + deployment.getName()),
//            true);
//      }
   }
}
