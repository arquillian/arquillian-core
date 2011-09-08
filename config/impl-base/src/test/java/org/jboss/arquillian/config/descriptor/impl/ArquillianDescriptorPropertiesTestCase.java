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
package org.jboss.arquillian.config.descriptor.impl;

import static org.jboss.arquillian.config.descriptor.impl.AssertXPath.assertXPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * ArquillianDescriptorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:ralf.battenfeld@bluewin.ch">Ralf Battenfeld</a>
 * @version $Revision: $
 */
public class ArquillianDescriptorPropertiesTestCase
{
   private static final String CONTAINER_NAME_1 = "jbossas-remote";
   private static final String CONTAINER_NAME_2 = "jbossas-embedded";
   private static final String CONTAINER_NAME_3 = "jbossas-managed";
   private static final String CONTAINER_NAME_4 = "jbossas-cloud";
   private static final String PROTOCOL_TYPE_1 = "Servlet 3.0";
   private static final String PROTOCOL_TYPE_2 = "Servlet 2.5";
   private static final String PROTOCOL_TYPE_3 = "EJB 3.0";
   private static final String GROUP_NAME_1 = "jbossas-remote-group";
   private static final String GROUP_NAME_2 = "jbossas-embedded-group";
   private static final String DEPENDENCY_1 = "org.test:test";
   private static final String DEPENDENCY_2 = "org.test:test2";
   private static final String EXTENSION_NAME_1 = "selenium";
   private static final String EXTENSION_NAME_2 = "performance";
   private static final String PROPERTY_NAME_1 = "test-name";
   private static final String PROPERTY_VALUE_1 = "test-value";
   private static final String PROPERTY_NAME_2 = "test2-name";
   private static final String PROPERTY_VALUE_2 = "test2-value";
   private static final String PROPERTY_NAME_3 = "test3-name";
   private static final String PROPERTY_VALUE_3 = "test3-value";   
   
   private static final Integer PROPERTY_INT_VALUE_1 = 10;
   
   // properties keys
   private static final String KEY_PROPERTY_VALUE_1 	= "property.value.1";
   private static final String KEY_PROPERTY_VALUE_2     = "property.value.2";
   private static final String KEY_PROPERTY_VALUE_3     = "property.value.3";
   private static final String KEY_PROPERTY_NAME_1      = "property.name.1";
   private static final String KEY_PROPERTY_NAME_2      = "property.name.2";
   private static final String KEY_CONTAINER_NAME_1 	= "container.name.1";
   private static final String KEY_CONTAINER_NAME_2 	= "container.name.2";
   private static final String KEY_CONTAINER_NAME_3     = "container.name.3";
   private static final String KEY_CONTAINER_NAME_4     = "container.name.4";
   
   private static final String KEY_DEPENDENCY_1         = "dependency.1";
   private static final String KEY_DEPENDENCY_2         = "dependency.2";
   
   private String desc;
   
   @After
   public void print() 
   {
      System.out.println(desc);
   }

   @Test
   public void shouldBeAbleToGenerateEmpty() throws Exception
   {
      desc = create().exportAsString();
   }

   @Test
   public void shouldBeAbleToSetEngineProperties() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      
      // add multiple times to see only one property added
      desc = create()
               .engine()
                  .deploymentExportPath(setPropKey(KEY_PROPERTY_VALUE_1))
                  .deploymentExportPath(setPropKey(KEY_PROPERTY_VALUE_1))
                  .maxTestClassesBeforeRestart(PROPERTY_INT_VALUE_1)
                  .maxTestClassesBeforeRestart(PROPERTY_INT_VALUE_1)
             .resolve()
             .exportAsString(); 

      assertXPath(desc, "/arquillian/engine/property[@name='deploymentExportPath']/text()", PROPERTY_VALUE_1);
      assertXPath(desc, "/arquillian/engine/property[@name='maxTestClassesBeforeRestart']/text()", PROPERTY_INT_VALUE_1);   
   
      ArquillianDescriptor descriptor = create(desc);
      
      Assert.assertEquals(PROPERTY_VALUE_1, descriptor.engine().getDeploymentExportPath());
      Assert.assertEquals(PROPERTY_INT_VALUE_1, descriptor.engine().getMaxTestClassesBeforeRestart());
   }
   
   @Test
   public void shouldReturnNullOnEnginePropertiesIfNotSet() throws Exception
   {
      // add multiple times to see only one property added
      desc = create()
               .engine()
             .exportAsString(); 

      ArquillianDescriptor descriptor = create(desc);
      
      Assert.assertNull(descriptor.engine().getDeploymentExportPath());
      Assert.assertNull(descriptor.engine().getMaxTestClassesBeforeRestart());
   }

   
   @Test
   public void shouldBeAbleToAddContainer() throws Exception
   {
	  System.setProperty(KEY_CONTAINER_NAME_1, CONTAINER_NAME_1);
	  System.setProperty(KEY_CONTAINER_NAME_2, CONTAINER_NAME_2);
	   
      desc = create()
            .container(setPropKey(KEY_CONTAINER_NAME_1)).setDefault()
            .container(setPropKey(KEY_CONTAINER_NAME_2)).resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/container/@qualifier", CONTAINER_NAME_1, CONTAINER_NAME_2);
      assertXPath(desc, "/arquillian/container[1]/@default", "true");
      
      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(2, descriptor.getContainers().size());
      Assert.assertEquals(CONTAINER_NAME_1, descriptor.getContainers().get(0).getContainerName());
      Assert.assertTrue(descriptor.getContainers().get(0).isDefault());
      Assert.assertEquals(CONTAINER_NAME_2, descriptor.getContainers().get(1).getContainerName());
   }

   @Test
   public void shouldBeAbleToAddContainerAndOverwrite() throws Exception
   {
	  System.setProperty(KEY_CONTAINER_NAME_1, CONTAINER_NAME_1);
	  System.setProperty(KEY_CONTAINER_NAME_2, CONTAINER_NAME_2);
	  
      desc = create()
            .container(CONTAINER_NAME_1).setDefault()
            .container(CONTAINER_NAME_1).setContainerName(setPropKey(KEY_CONTAINER_NAME_2))
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/container/@qualifier", CONTAINER_NAME_2);
      assertXPath(desc, "/arquillian/container/@default", "true");

      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getContainers().size());
      Assert.assertEquals(CONTAINER_NAME_2, descriptor.getContainers().get(0).getContainerName());
      Assert.assertTrue(descriptor.getContainers().get(0).isDefault());
   }

   @Test
   public void shouldBeAbleToAddDefaultProtocol() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      
      desc = create()
            .defaultProtocol(PROTOCOL_TYPE_1)
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/defaultProtocol/@type", PROTOCOL_TYPE_1);
      assertXPath(desc, "/arquillian/defaultProtocol/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/defaultProtocol/property/text()", PROPERTY_VALUE_1);
      
      ArquillianDescriptor descriptor = create(desc);
      Assert.assertNotNull(descriptor.getDefaultProtocol());
      Assert.assertEquals(PROTOCOL_TYPE_1, descriptor.getDefaultProtocol().getType());
      Assert.assertEquals(PROPERTY_VALUE_1, descriptor.getDefaultProtocol().getProperties().get(PROPERTY_NAME_1));
   }

  
   @Test
   public void shouldBeAbleToAddDefaultProtocolAndOverwriteProperty() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      System.setProperty(KEY_PROPERTY_VALUE_2, PROPERTY_VALUE_2);
      
      desc = create()
            .defaultProtocol(PROTOCOL_TYPE_1)
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_2))
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/defaultProtocol/@type", PROTOCOL_TYPE_1);
      assertXPath(desc, "/arquillian/defaultProtocol/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/defaultProtocol/property/text()", PROPERTY_VALUE_2);

      ArquillianDescriptor descriptor = create(desc);
      Assert.assertNotNull(descriptor.getDefaultProtocol());
      Assert.assertEquals(PROTOCOL_TYPE_1, descriptor.getDefaultProtocol().getType());
      Assert.assertEquals(1, descriptor.getDefaultProtocol().getProperties().size());
      Assert.assertEquals(PROPERTY_VALUE_2, descriptor.getDefaultProtocol().getProperties().get(PROPERTY_NAME_1));
   }

   @Test
   public void shouldBeAbleToAddContainerWithDependencies() throws Exception
   {
      System.setProperty(KEY_DEPENDENCY_1, DEPENDENCY_1);
      System.setProperty(KEY_DEPENDENCY_2, DEPENDENCY_2);
      
      desc = create()
            .container(CONTAINER_NAME_1)
            .dependency(setPropKey(KEY_DEPENDENCY_1))
            .dependency(setPropKey(KEY_DEPENDENCY_2)).resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/container/dependencies/dependency", DEPENDENCY_1, DEPENDENCY_2);
      
      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getContainers().size());
      Assert.assertEquals(CONTAINER_NAME_1, descriptor.getContainers().get(0).getContainerName());
      
      Assert.assertEquals(DEPENDENCY_1, descriptor.getContainers().get(0).getDependencies().get(0));
      Assert.assertEquals(DEPENDENCY_2, descriptor.getContainers().get(0).getDependencies().get(1));
   }

   @Test
   public void shouldBeAbleToAddContainerWithDependenciesAndOverwrite() throws Exception
   {
      System.setProperty(KEY_DEPENDENCY_1, DEPENDENCY_1);
      
      desc = create()
            .container(CONTAINER_NAME_1)
            .dependency(setPropKey(KEY_DEPENDENCY_1))
            .dependency(setPropKey(KEY_DEPENDENCY_1)).resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/container/dependencies/dependency", DEPENDENCY_1);

      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getContainers().size());
      Assert.assertEquals(CONTAINER_NAME_1, descriptor.getContainers().get(0).getContainerName());
      Assert.assertEquals(1, descriptor.getContainers().get(0).getDependencies().size());
      Assert.assertEquals(DEPENDENCY_1, descriptor.getContainers().get(0).getDependencies().get(0));
   }

   @Test
   public void shouldBeAbleToAddContainerWithMultipleProtocols() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      System.setProperty(KEY_PROPERTY_VALUE_2, PROPERTY_VALUE_2);
      
      desc = create()
            .container(CONTAINER_NAME_1)
               .protocol(PROTOCOL_TYPE_1)
                  .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
               .protocol(PROTOCOL_TYPE_2)
                  .property(PROPERTY_NAME_2, setPropKey(KEY_PROPERTY_VALUE_2))
            .resolve().exportAsString();
           
      assertXPath(desc, "/arquillian/container/@qualifier", CONTAINER_NAME_1);
      assertXPath(desc, "/arquillian/container/protocol[1]/@type", PROTOCOL_TYPE_1);
      assertXPath(desc, "/arquillian/container/protocol[1]/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/container/protocol[1]/property/text()", PROPERTY_VALUE_1);

      assertXPath(desc, "/arquillian/container/protocol[2]/@type", PROTOCOL_TYPE_2);
      assertXPath(desc, "/arquillian/container/protocol[2]/property/@name", PROPERTY_NAME_2);
      assertXPath(desc, "/arquillian/container/protocol[2]/property/text()", PROPERTY_VALUE_2);

      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getContainers().size());
      Assert.assertEquals(CONTAINER_NAME_1, descriptor.getContainers().get(0).getContainerName());
      
      Assert.assertEquals(2, descriptor.getContainers().get(0).getProtocols().size());
      Assert.assertEquals(PROTOCOL_TYPE_1, descriptor.getContainers().get(0).getProtocols().get(0).getType());
      Assert.assertEquals(PROPERTY_VALUE_1, descriptor.getContainers().get(0).getProtocols().get(0).getProtocolProperties().get(PROPERTY_NAME_1));
      Assert.assertEquals(PROTOCOL_TYPE_2, descriptor.getContainers().get(0).getProtocols().get(1).getType());
      Assert.assertEquals(PROPERTY_VALUE_2, descriptor.getContainers().get(0).getProtocols().get(1).getProtocolProperties().get(PROPERTY_NAME_2));
   }
   
   @Test
   public void shouldBeAbleToAddContainerAndOverwriteProtocol() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      System.setProperty(KEY_PROPERTY_VALUE_2, PROPERTY_VALUE_2);
      
      desc = create()
            .container(CONTAINER_NAME_1)
               .protocol(PROTOCOL_TYPE_1)
                  .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
               .protocol(PROTOCOL_TYPE_1)
                  .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_2))
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/container/@qualifier", CONTAINER_NAME_1);
      assertXPath(desc, "/arquillian/container/protocol/@type", PROTOCOL_TYPE_1);
      assertXPath(desc, "/arquillian/container/protocol/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/container/protocol/property/text()", PROPERTY_VALUE_2);

      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getContainers().size());
      Assert.assertEquals(1, descriptor.getContainers().get(0).getProtocols().size());
      Assert.assertEquals(PROTOCOL_TYPE_1, descriptor.getContainers().get(0).getProtocols().get(0).getType());
      Assert.assertEquals(PROPERTY_VALUE_2, descriptor.getContainers().get(0).getProtocols().get(0).getProtocolProperties().get(PROPERTY_NAME_1));
   }

   @Test
   public void shouldBeAbleToAddContainerWithConfiguration() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      System.setProperty(KEY_PROPERTY_VALUE_2, PROPERTY_VALUE_2);
      
      desc = create()
            .container(CONTAINER_NAME_1)
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
            .container(CONTAINER_NAME_2)
               .property(PROPERTY_NAME_2, setPropKey(KEY_PROPERTY_VALUE_2))
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/container[1]/@qualifier", CONTAINER_NAME_1);
      
      assertXPath(desc, "/arquillian/container[1]/configuration/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/container[1]/configuration/property/text()", PROPERTY_VALUE_1);

      assertXPath(desc, "/arquillian/container[2]/@qualifier", CONTAINER_NAME_2);
      assertXPath(desc, "/arquillian/container[2]/configuration/property/@name", PROPERTY_NAME_2);
      assertXPath(desc, "/arquillian/container[2]/configuration/property/text()", PROPERTY_VALUE_2);

      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(2, descriptor.getContainers().size());
      Assert.assertEquals(CONTAINER_NAME_1, descriptor.getContainers().get(0).getContainerName());
      Assert.assertEquals(PROPERTY_VALUE_1, descriptor.getContainers().get(0).getContainerProperties().get(PROPERTY_NAME_1));
      Assert.assertEquals(CONTAINER_NAME_2, descriptor.getContainers().get(1).getContainerName());
      Assert.assertEquals(PROPERTY_VALUE_2, descriptor.getContainers().get(1).getContainerProperties().get(PROPERTY_NAME_2));
   }
   
   @Test
   public void shouldBeAbleToAddContainerWithConfigurationAndOverwriteProperty() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      System.setProperty(KEY_PROPERTY_VALUE_2, PROPERTY_VALUE_2);
      
      desc = create()
            .container(CONTAINER_NAME_1)
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_2))
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/container[1]/@qualifier", CONTAINER_NAME_1);
      
      assertXPath(desc, "/arquillian/container[1]/configuration/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/container[1]/configuration/property/text()", PROPERTY_VALUE_2);

      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getContainers().size());
      Assert.assertEquals(CONTAINER_NAME_1, descriptor.getContainers().get(0).getContainerName());
      Assert.assertEquals(1, descriptor.getContainers().get(0).getContainerProperties().size());
      Assert.assertEquals(PROPERTY_VALUE_2, descriptor.getContainers().get(0).getContainerProperties().get(PROPERTY_NAME_1));
   }
   
   @Test
   public void shouldBeAbleToAddGroupWithContainer() throws Exception
   {
      System.setProperty(KEY_CONTAINER_NAME_1, CONTAINER_NAME_1);
      System.setProperty(KEY_CONTAINER_NAME_2, CONTAINER_NAME_2);
      System.setProperty(KEY_CONTAINER_NAME_3, CONTAINER_NAME_3);
      
      desc = create()
            .group(GROUP_NAME_1)
               .setGroupDefault()
               .container(setPropKey(KEY_CONTAINER_NAME_1))
               .container(setPropKey(KEY_CONTAINER_NAME_2))
            .group(GROUP_NAME_2)
               .container(setPropKey(KEY_CONTAINER_NAME_3)).resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/group/@qualifier", GROUP_NAME_1, GROUP_NAME_2);
      assertXPath(desc, "/arquillian/group[1]/@default", true);
      assertXPath(desc, "/arquillian/group/container/@qualifier", CONTAINER_NAME_1, CONTAINER_NAME_2, CONTAINER_NAME_3);
      
      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(2, descriptor.getGroups().size());
      Assert.assertEquals(2, descriptor.getGroups().get(0).getGroupContainers().size());
      Assert.assertEquals(1, descriptor.getGroups().get(1).getGroupContainers().size());
      Assert.assertEquals(GROUP_NAME_1, descriptor.getGroups().get(0).getGroupName());
      Assert.assertEquals(CONTAINER_NAME_1, descriptor.getGroups().get(0).getGroupContainers().get(0).getContainerName());
      Assert.assertEquals(CONTAINER_NAME_2, descriptor.getGroups().get(0).getGroupContainers().get(1).getContainerName());
      Assert.assertEquals(GROUP_NAME_2, descriptor.getGroups().get(1).getGroupName());
      Assert.assertEquals(CONTAINER_NAME_3, descriptor.getGroups().get(1).getGroupContainers().get(0).getContainerName());
   }
   
   @Test
   public void shouldBeAbleToAddGroupWithContainerAndOverwriteContainer() throws Exception
   {
      System.setProperty(KEY_CONTAINER_NAME_1, CONTAINER_NAME_1);
      
      desc = create()
            .group(GROUP_NAME_1)
               .container(setPropKey(KEY_CONTAINER_NAME_1))
               .container(setPropKey(KEY_CONTAINER_NAME_1))
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/group/@qualifier", GROUP_NAME_1);
      assertXPath(desc, "/arquillian/group/container/@qualifier", CONTAINER_NAME_1);
      
      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getGroups().size());
      Assert.assertEquals(1, descriptor.getGroups().get(0).getGroupContainers().size());
      Assert.assertEquals(CONTAINER_NAME_1, descriptor.getGroups().get(0).getGroupContainers().get(0).getContainerName());
   }

   @Test
   public void shouldBeAbleToAddExtension() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      System.setProperty(KEY_PROPERTY_VALUE_2, PROPERTY_VALUE_2);
      System.setProperty(KEY_PROPERTY_VALUE_3, PROPERTY_VALUE_3);
      
      desc = create()
            .extension(EXTENSION_NAME_1)
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
               .property(PROPERTY_NAME_2, setPropKey(KEY_PROPERTY_VALUE_2))
            .extension(EXTENSION_NAME_2)
               .property(PROPERTY_NAME_3, setPropKey(KEY_PROPERTY_VALUE_3)).resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/extension/@qualifier", EXTENSION_NAME_1, EXTENSION_NAME_2);
      assertXPath(desc, "/arquillian/extension[1]/property[1]/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/extension[1]/property[1]/text()", PROPERTY_VALUE_1);
      assertXPath(desc, "/arquillian/extension[1]/property[2]/@name", PROPERTY_NAME_2);
      assertXPath(desc, "/arquillian/extension[1]/property[2]/text()", PROPERTY_VALUE_2);
      
      assertXPath(desc, "/arquillian/extension[2]/property/@name", PROPERTY_NAME_3);
      assertXPath(desc, "/arquillian/extension[2]/property/text()", PROPERTY_VALUE_3);
      
      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(2, descriptor.getExtensions().size());
      Assert.assertEquals(EXTENSION_NAME_1, descriptor.getExtensions().get(0).getExtensionName());
      Assert.assertEquals(2, descriptor.getExtensions().get(0).getExtensionProperties().size());
      Assert.assertEquals(PROPERTY_VALUE_1, descriptor.getExtensions().get(0).getExtensionProperties().get(PROPERTY_NAME_1));
      Assert.assertEquals(PROPERTY_VALUE_2, descriptor.getExtensions().get(0).getExtensionProperties().get(PROPERTY_NAME_2));

      Assert.assertEquals(EXTENSION_NAME_2, descriptor.getExtensions().get(1).getExtensionName());
      Assert.assertEquals(1, descriptor.getExtensions().get(1).getExtensionProperties().size());
      Assert.assertEquals(PROPERTY_VALUE_3, descriptor.getExtensions().get(1).getExtensionProperties().get(PROPERTY_NAME_3));
   }
   
   @Test
   public void shouldBeAbleToRenameExtension() throws Exception
   {
      desc = create()
            .extension(EXTENSION_NAME_1)
               .property(PROPERTY_NAME_1, PROPERTY_VALUE_1)
            .extension(EXTENSION_NAME_1)
               .setExtensionName(EXTENSION_NAME_2)
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/extension/@qualifier", EXTENSION_NAME_2);
      
      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getExtensions().size());
      Assert.assertEquals(EXTENSION_NAME_2, descriptor.getExtensions().get(0).getExtensionName());
      Assert.assertEquals(1, descriptor.getExtensions().get(0).getExtensionProperties().size());
      Assert.assertEquals(PROPERTY_VALUE_1, descriptor.getExtensions().get(0).getExtensionProperties().get(PROPERTY_NAME_1));
   }

   @Test
   public void shouldBeAbleToAddExtensionAndOverwriteProperty() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      System.setProperty(KEY_PROPERTY_VALUE_2, PROPERTY_VALUE_2);
      
      desc = create()
            .extension(EXTENSION_NAME_1)
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_2))
            .resolve().exportAsString();
      
      assertXPath(desc, "/arquillian/extension/@qualifier", EXTENSION_NAME_1);
      assertXPath(desc, "/arquillian/extension/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/extension/property/text()", PROPERTY_VALUE_2);

      ArquillianDescriptor descriptor = create(desc);
      Assert.assertEquals(1, descriptor.getExtensions().size());
      Assert.assertEquals(EXTENSION_NAME_1, descriptor.getExtensions().get(0).getExtensionName());
      Assert.assertEquals(1, descriptor.getExtensions().get(0).getExtensionProperties().size());
      Assert.assertEquals(PROPERTY_VALUE_2, descriptor.getExtensions().get(0).getExtensionProperties().get(PROPERTY_NAME_1));
   }

   @Test
   public void shouldBeAbleToAddEverything() throws Exception
   {
      System.setProperty(KEY_PROPERTY_VALUE_1, PROPERTY_VALUE_1);
      System.setProperty(KEY_PROPERTY_VALUE_2, PROPERTY_VALUE_2);
      System.setProperty(KEY_PROPERTY_VALUE_3, PROPERTY_VALUE_3);
      
      desc = create()
            .defaultProtocol(PROTOCOL_TYPE_1)
               .property(PROPERTY_VALUE_3, setPropKey(KEY_PROPERTY_VALUE_3))
            .container(CONTAINER_NAME_1)
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
               .dependency(DEPENDENCY_1)
               .protocol(PROTOCOL_TYPE_1)
                  .property(PROPERTY_NAME_2, setPropKey(KEY_PROPERTY_VALUE_2))
            .group(GROUP_NAME_1)
               .container(CONTAINER_NAME_2)
                  .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
                  .dependency(DEPENDENCY_2)
                  .protocol(PROTOCOL_TYPE_2)
                     .property(PROPERTY_NAME_3, setPropKey(KEY_PROPERTY_VALUE_3))
            .group(GROUP_NAME_2)
               .container(CONTAINER_NAME_3)
                  .protocol(PROTOCOL_TYPE_3)
                     .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_1))
            .container(CONTAINER_NAME_4)
            .extension(EXTENSION_NAME_1) 
               .property(PROPERTY_NAME_1, setPropKey(KEY_PROPERTY_VALUE_2))
            .resolve().exportAsString();
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper --------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private ArquillianDescriptor create()
   {
      return Descriptors.create(ArquillianDescriptor.class);
   }

   private ArquillianDescriptor create(String xml) throws Exception
   {
      validateXML(desc);
      
      return Descriptors.importAs(ArquillianDescriptor.class).from(xml);
   }

   private void validateXML(String xml) throws Exception
   {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
      dbf.setValidating(true); 
      dbf.setNamespaceAware(true);
      dbf.setAttribute( 
          "http://java.sun.com/xml/jaxp/properties/schemaLanguage", 
          "http://www.w3.org/2001/XMLSchema"); 
      DocumentBuilder db = dbf.newDocumentBuilder(); 
      db.setErrorHandler(new ErrorHandler()
      {
         @Override
         public void warning(SAXParseException exception) throws SAXException
         {
            throw exception;
         }
         
         @Override
         public void fatalError(SAXParseException exception) throws SAXException
         {
            throw exception;
         }
         
         @Override
         public void error(SAXParseException exception) throws SAXException
         {
            throw exception;
         }
      });
      db.setEntityResolver(new EntityResolver()
      {
         @Override
         public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
         {
            if("http://jboss.org/schema/arquillian/arquillian_1_0.xsd".equals(systemId))
            {
               return new InputSource(this.getClass().getClassLoader().getResourceAsStream("arquillian_1_0.xsd"));
            }
            return null;
         }
      }); 
      db.parse(new ByteArrayInputStream(xml.getBytes()));
   }
   
   private String setPropKey(String propKey) {
	   return String.format("${%s}", propKey);
   }
}
