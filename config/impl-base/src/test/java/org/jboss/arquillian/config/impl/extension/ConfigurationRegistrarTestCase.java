/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConfigurationRegistrarTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ConfigurationRegistrarTestCase extends AbstractManagerTestBase
{
   @Inject
   private Instance<Injector> injectorInst;

   @Inject
   private Instance<ArquillianDescriptor> descInst;

   private ConfigurationRegistrar registrar;

   @Before
   public void injectConfigurationRegistrar()
   {
      registrar = injectorInst.get().inject(new ConfigurationRegistrar());
   }

   @Test
   public void shouldBeAbleToLoadEmptyDefaultConfiguration() throws Exception
   {
      registrar.loadConfiguration(new ManagerStarted());
      ArquillianDescriptor desc = descInst.get();

      Assert.assertEquals(0, desc.getContainers().size());
      Assert.assertEquals(0, desc.getGroups().size());
      Assert.assertEquals(0, desc.getExtensions().size());
   }

   @Test
   public void shouldBeAbleToLoadConfiguredXMLFileResource() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
            "src/test/resources/registrar_tests/named_arquillian.xml",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  registrar.loadConfiguration(new ManagerStarted());
                  ArquillianDescriptor desc = descInst.get();

                  Assert.assertEquals(1, desc.getContainers().size());
                  Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                  // verify mode = class, override test will set it to suite
                  Assert.assertEquals("class", desc.getContainers().get(0).getMode());
               }
            });
   }

   @Test
   public void shouldBeAbleToLoadConfiguredXMLClasspathResource() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
            "registrar_tests/named_arquillian.xml",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  registrar.loadConfiguration(new ManagerStarted());
                  ArquillianDescriptor desc = descInst.get();

                  Assert.assertEquals(1, desc.getContainers().size());
                  Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                  // verify mode = class, override test will set it to suite
                  Assert.assertEquals("class", desc.getContainers().get(0).getMode());
               }
            });
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnMissingConfiguredXMLResource() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
            "registrar_tests/named_arquillian_SHOULD_NOT_BE_FOUND_.xml",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  registrar.loadConfiguration(new ManagerStarted());
               }
            });
   }

   @Test
   public void shouldBeAbleToLoadConfiguredPropertiesFileResource() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
            "src/test/resources/registrar_tests/named_arquillian.properties",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  registrar.loadConfiguration(new ManagerStarted());
                  ArquillianDescriptor desc = descInst.get();

                  Assert.assertEquals(1, desc.getContainers().size());
                  Assert.assertEquals("B", desc.getContainers().get(0).getContainerName());
               }
            });
   }

   @Test
   public void shouldBeAbleToLoadConfiguredPropertiesClasspathResource() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
            "registrar_tests/named_arquillian.properties",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  registrar.loadConfiguration(new ManagerStarted());
                  ArquillianDescriptor desc = descInst.get();

                  Assert.assertEquals(1, desc.getContainers().size());
                  Assert.assertEquals("B", desc.getContainers().get(0).getContainerName());
                  Assert.assertEquals("manual", desc.getContainers().get(0).getMode());
               }
            });
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldThrowExceptionOnMissingConfiguredPropertiesResource() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
            "registrar_tests/named_arquillian_SHOULD_NOT_BE_FOUND_.properties",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  registrar.loadConfiguration(new ManagerStarted());
               }
            });
   }

   @Test
   public void shouldBeAbleToAddSystemProperties() throws Exception
   {
      validate(
            "arq.container.C.mode",
            "manual",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  registrar.loadConfiguration(new ManagerStarted());
                  ArquillianDescriptor desc = descInst.get();

                  Assert.assertEquals(1, desc.getContainers().size());
                  Assert.assertEquals("C", desc.getContainers().get(0).getContainerName());
                  Assert.assertEquals("manual", desc.getContainers().get(0).getMode());
               }
            });
   }

   @Test
   public void shouldBeAbleToOverrideWithSystemProperties() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
            "registrar_tests/named_arquillian.xml",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  ConfigurationRegistrarTestCase.validate(
                        "arq.container.A.mode",
                        "suite",
                        new AssertCallback()
                        {
                           @Override
                           public void validate()
                           {
                              registrar.loadConfiguration(new ManagerStarted());
                              ArquillianDescriptor desc = descInst.get();

                              Assert.assertEquals(1, desc.getContainers().size());
                              Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                              Assert.assertEquals("suite", desc.getContainers().get(0).getMode());
                           }
                        });
               }
            });
   }

   @Test
   public void shouldBeAbleToAddToXMLWithProperties() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
            "registrar_tests/named_arquillian.xml",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  ConfigurationRegistrarTestCase.validate(
                        ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                        "registrar_tests/named_arquillian.properties",
                        new AssertCallback()
                        {
                           @Override
                           public void validate()
                           {
                              registrar.loadConfiguration(new ManagerStarted());
                              ArquillianDescriptor desc = descInst.get();

                              Assert.assertEquals(2, desc.getContainers().size());
                              Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                              Assert.assertEquals("B", desc.getContainers().get(1).getContainerName());
                           }
                        });
               }
            });
   }

   @Test
   public void shouldBeAbleToOverrideToXMLWithProperties() throws Exception
   {
      validate(
            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
            "registrar_tests/named_arquillian.xml",
            new AssertCallback()
            {
               @Override
               public void validate()
               {
                  ConfigurationRegistrarTestCase.validate(
                        ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                        "registrar_tests/override_named_arquillian.properties",
                        new AssertCallback()
                        {
                           @Override
                           public void validate()
                           {
                              registrar.loadConfiguration(new ManagerStarted());
                              ArquillianDescriptor desc = descInst.get();

                              Assert.assertEquals(1, desc.getContainers().size());
                              Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                              Assert.assertEquals("suite", desc.getContainers().get(0).getMode());
                           }
                        });
               }
            });
   }

   static void validate(String property, String value, AssertCallback callback)
   {
      try
      {
         System.setProperty(property, value);
         callback.validate();
      }
      finally
      {
         System.clearProperty(property);
      }
   }

   public interface AssertCallback
   {
      void validate();
   }
}