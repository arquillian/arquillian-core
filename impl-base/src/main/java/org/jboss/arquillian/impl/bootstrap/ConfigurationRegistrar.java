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
package org.jboss.arquillian.impl.bootstrap;

import java.io.InputStream;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.core.event.ManagerStarted;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * Configurator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ConfigurationRegistrar
{
   static final String ARQUILLIAN_XML_PROPERTY = "arquillian.xml"; 
   static final String ARQUILLIAN_XML_DEFAULT = "arquillian.xml";
   
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
      
      descriptorInst.set(descriptor);
   }
   
   private InputStream loadArquillianXml()
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
}
