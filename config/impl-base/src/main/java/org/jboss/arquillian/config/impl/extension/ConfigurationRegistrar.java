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

import static org.jboss.arquillian.config.impl.extension.ConfigurationSysPropResolver.resolveSystemProperties;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

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
   public static final String ARQUILLIAN_XML_PROPERTY = "arquillian.xml";
   public static final String ARQUILLIAN_XML_DEFAULT = "arquillian.xml";

   public static final String ARQUILLIAN_PROP_PROPERTY = "arquillian.properties";
   public static final String ARQUILLIAN_PROP_DEFAULT = "arquillian.properties";

   private Map<String, String> systemEnvironmentVars = System.getenv();

   @Inject @ApplicationScoped
   private InstanceProducer<ArquillianDescriptor> descriptorInst;

   public void loadConfiguration(@Observes ManagerStarted event)
   {
      final InputStream input = FileUtils.loadArquillianXml(ARQUILLIAN_XML_PROPERTY, ARQUILLIAN_XML_DEFAULT);

      //First arquillian.xml is resolved
      final ArquillianDescriptor descriptor = resolveDescriptor(input);

      //Second arquillian.properties file and system properties are applied
      final PropertiesParser propertiesParser = new PropertiesParser();
      propertiesParser.addProperties(
              descriptor,
              FileUtils.loadArquillianProperties(ARQUILLIAN_PROP_PROPERTY, ARQUILLIAN_PROP_DEFAULT));

      //Fourth arquillian properties from system environment variables are applied
      Properties envProperties = new Properties();
      envProperties.putAll(systemEnvironmentVars);
      propertiesParser.addProperties(descriptor, envProperties);

      //Placeholder resolver
      final ArquillianDescriptor resolvedDesc = resolveSystemProperties(descriptor);

      descriptorInst.set(resolvedDesc);
   }

   private ArquillianDescriptor resolveDescriptor(final InputStream input)
   {
      final ArquillianDescriptor descriptor;

      if(input != null)
      {
         descriptor = Descriptors.importAs(ArquillianDescriptor.class)
                                          .fromStream(input);
      }
      else
      {
         descriptor = Descriptors.create(ArquillianDescriptor.class);
      }
      return descriptor;
   }

   //testing purposes
   void setEnvironmentVariables(Map<String, String> variables)
   {
      this.systemEnvironmentVars = variables;
   }
}