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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.spi.ApplicationArchiveGenerator;
import org.jboss.arquillian.spi.ApplicationArchiveProcessor;
import org.jboss.arquillian.spi.AuxiliaryArchiveAppender;
import org.jboss.arquillian.spi.AuxiliaryArchiveProcessor;
import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * Responsible for 
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClientDeploymentGenerator implements DeploymentGenerator
{
   private ServiceLoader serviceLoader;
   
   public ClientDeploymentGenerator(ServiceLoader serviceLoader)
   {
      Validate.notNull(serviceLoader, "ServiceLoader must be specified");
      
      this.serviceLoader = serviceLoader;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.DeploymentGenerator#generate(java.lang.Class)
    */
   @Override
   public Archive<?> generate(Class<?> testCase)
   {
      Validate.notNull(testCase, "TestCase must be specified");

      DeploymentPackager packager = serviceLoader.onlyOne(DeploymentPackager.class);

      Archive<?> applicationArchive = serviceLoader.onlyOne(ApplicationArchiveGenerator.class).generateApplicationArchive(testCase);
      applyApplicationProcessors(applicationArchive, testCase);
      
      List<Archive<?>> auxiliaryArchives = loadAuxiliaryArchives();
      applyAuxiliaryProcessors(auxiliaryArchives);

      return packager.generateDeployment(applicationArchive, auxiliaryArchives);
   }
   
   private List<Archive<?>> loadAuxiliaryArchives() 
   {
      List<Archive<?>> archives = new ArrayList<Archive<?>>();
      Collection<AuxiliaryArchiveAppender> archiveAppenders = serviceLoader.all(AuxiliaryArchiveAppender.class);
   
      for(AuxiliaryArchiveAppender archiveAppender : archiveAppenders)
      {
         archives.add(archiveAppender.createAuxiliaryArchive());
      }
      return archives;
   }

   private void applyApplicationProcessors(Archive<?> applicationArchive, Class<?> testClass)
   {
      Collection<ApplicationArchiveProcessor> processors = serviceLoader.all(ApplicationArchiveProcessor.class);
      for(ApplicationArchiveProcessor processor : processors)
      {
         processor.process(applicationArchive, testClass);
      }
   }
   
   private void applyAuxiliaryProcessors(List<Archive<?>> auxiliaryArchives)
   {
      Collection<AuxiliaryArchiveProcessor> processors = serviceLoader.all(AuxiliaryArchiveProcessor.class);
      for(AuxiliaryArchiveProcessor processor : processors)
      {
         for(Archive<?> auxiliaryArchive : auxiliaryArchives)
         {
            processor.process(auxiliaryArchive);
         }
      }
   }
}
