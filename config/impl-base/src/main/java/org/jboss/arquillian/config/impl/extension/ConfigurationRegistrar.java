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
package org.jboss.arquillian.config.impl.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * Configurator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ConfigurationRegistrar
{
   private static final Logger log = Logger.getLogger(ConfigurationRegistrar.class.getName());

   static final String ARQUILLIAN_XML_PROPERTY = "arquillian.xml"; 
   static final String ARQUILLIAN_XML_DEFAULT = "arquillian.xml";
   
   static final String ARQUILLIAN_XML_FOLDER_PROPERTY = "arquillian.xml.folder.path";
   static final String ARQUILLIAN_XML_FOLDER_DEFAULT = System.getProperty("user.home") + File.separator +  ".arquillian";

   @Inject @ApplicationScoped
   private InstanceProducer<ArquillianDescriptor> descriptorInst;

   public void loadConfiguration(@Observes ManagerStarted event)
   {
      ArquillianDescriptor descriptor;
      
      InputStream input = loadArquillianXml();
      if(input != null)
      {
         descriptor = Descriptors.importAs(ArquillianDescriptor.class)
                                          .from(input);
      }
      else 
      {
         descriptor = Descriptors.create(ArquillianDescriptor.class);
      }
      
      final ArquillianDescriptor resolvedDesc = ConfigurationSysPropResolver.resolveSystemProperties(descriptor);
      descriptorInst.set(resolvedDesc);
   }
   
   private InputStream loadArquillianXml()
   {
      InputStream arquillianXml = loadLocalArquillianXml();
      if (arquillianXml == null)
         return loadGlobalArquillianXml();

      return arquillianXml;
   }

   private InputStream loadLocalArquillianXml()
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      return classLoader.getResourceAsStream(getConfigFileName());
   }
   
   private String getConfigFileName()
   {
      String name = System.getProperty(ARQUILLIAN_XML_PROPERTY);
      if(name == null)
      {
         name = ARQUILLIAN_XML_DEFAULT;
      }
      return name;
   }

   private InputStream loadGlobalArquillianXml()
   {
      try
      {
         return getGlobalArquillianXml();
      }
      catch (FileNotFoundException e)
      {
         log.log(Level.WARNING, "Exception while reading global configuration", e);
         return null;
      }
   }

   private InputStream getGlobalArquillianXml() throws FileNotFoundException
   {
      File globalFolder = new File(getConfigFolderPath());
      if (globalFolder.exists()) {
         File globalArquillianXml = new File(globalFolder + File.separator + getConfigFileName());
         if (globalArquillianXml.exists()) {
            return new FileInputStream(globalArquillianXml);
         }
      }
      return null;
   }

   private String getConfigFolderPath()
   {
      String folderPath = System.getProperty(ARQUILLIAN_XML_FOLDER_PROPERTY);

      if (folderPath == null)
         return ARQUILLIAN_XML_FOLDER_DEFAULT;

      return folderPath;
   }

}
