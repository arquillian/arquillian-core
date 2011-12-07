/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.impl.AssertXPath;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Davide D'Alto
 */
public class ConfigurationFolderDoesNotExistsRegistrarTestCase extends AbstractManagerTestBase
{
   @Inject
   private Instance<ArquillianDescriptor> arquillianXmlDesc;

   @BeforeClass
   public static void setSysprops()
   {
      System.setProperty(ConfigurationRegistrar.ARQUILLIAN_XML_FOLDER_PROPERTY, "WRONG_PATH");
   }

   @AfterClass
   public static void clearSysprops()
   {
      System.clearProperty(ConfigurationRegistrar.ARQUILLIAN_XML_FOLDER_PROPERTY);
   }

   @Test
   public void shouldUseDefaultDescriptorWhenFolderDoesnNotExists() throws Exception
   {
      final String xml = arquillianXmlDesc.get().exportAsString();
      AssertXPath.assertXPath(xml, "/arquillian/*");
   }

   @Override
   protected void addExtensions(final List<Class<?>> extensions)
   {
      extensions.add(ConfigurationRegistrar.class);
      super.addExtensions(extensions);
   }
}