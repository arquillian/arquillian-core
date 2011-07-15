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
package org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.impl.application;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.ApplicationDescriptor;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.SecurityRole;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.WebModule;
import org.jboss.shrinkwrap.descriptor.spi.DescriptorExporter;
import org.jboss.shrinkwrap.descriptor.spi.Node;
import org.jboss.shrinkwrap.descriptor.spi.NodeProviderImplBase;
import org.jboss.shrinkwrap.descriptor.spi.xml.dom.XmlDomExporter;

/**
 * ApplicationDescriptorImpl
 * 
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ApplicationDescriptorImpl extends NodeProviderImplBase implements ApplicationDescriptor
{
   // -------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   private final Node model;

   // -------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   public ApplicationDescriptorImpl(String descriptorName)
   {
      this(descriptorName, new Node("application")
               .attribute("xmlns", "http://java.sun.com/xml/ns/javaee")
               .attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));

      version("6");
   }

   public ApplicationDescriptorImpl(String descriptorName, Node model)
   {
      super(descriptorName);
      this.model = model;
   }

   // -------------------------------------------------------------------------------------||
   // API --------------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#description()
    */
   @Override
   public ApplicationDescriptor description(String description)
   {
      model.getOrCreate("description").text(description);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#displayName(java.lang.String)
    */
   @Override
   public ApplicationDescriptor displayName(String displayName)
   {
      model.getOrCreate("display-name").text(displayName);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#libraryDirectory(java.lang.String)
    */
   @Override
   public ApplicationDescriptor libraryDirectory(String libraryDirectory)
   {
      model.getOrCreate("library-directory").text(libraryDirectory);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#ejbModule(java.lang.String)
    */
   @Override
   public ApplicationDescriptor ejbModule(String uri)
   {
      model.create("module").create("ejb").text(uri);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#javaModule(java.lang.String)
    */
   @Override
   public ApplicationDescriptor javaModule(String uri)
   {
      model.create("module").create("java").text(uri);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#connectorModule(java.lang.String)
    */
   @Override
   public ApplicationDescriptor connectorModule(String uri)
   {
      model.create("module").create("connector").text(uri);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#webModule(java.lang.String,
    * java.lang.String)
    */
   @Override
   public ApplicationDescriptor webModule(String uri, String contextRoot)
   {
      Node web = model.create("module").create("web");
      web.create("web-uri").text(uri);
      web.create("context-root").text(contextRoot);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#version(java.lang.String)
    */
   @Override
   public ApplicationDescriptor version(String version)
   {
      model.attribute("version", version);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#securityRole(java.lang.String)
    */
   @Override
   public ApplicationDescriptor securityRole(String roleName)
   {
      return securityRole(roleName, null);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#securityRole(java.lang.String,
    * java.lang.String)
    */
   @Override
   public ApplicationDescriptor securityRole(String roleName, String description)
   {
      Node security = model.create("security-role");
      if (roleName != null)
      {
         security.create("role-name").text(roleName);
      }
      if (description != null)
      {
         security.create("description").text(description);
      }
      return this;
   }

   // -------------------------------------------------------------------------------------||
   // Required Implementations - NodeProviderImplBase ------------------------------------||
   // -------------------------------------------------------------------------------------||

   @Override
   public Node getRootNode()
   {
      return model;
   }

   @Override
   protected DescriptorExporter getExporter()
   {
      return new XmlDomExporter();
   }

   @Override
   public String getVersion()
   {
      return model.attributes().get("version");
   }

   @Override
   public String getDisplayName()
   {
      return model.attributes().get("display-name");
   }

   @Override
   public String getDescription()
   {
      return model.attributes().get("description");
   }

   @Override
   public String getLibraryDirectory()
   {
      return model.attributes().get("library-directory");
   }

   @Override
   public List<WebModule> getWebModules()
   {
      List<WebModule> result = new ArrayList<WebModule>();

      List<Node> webModules = model.get("module/web");
      for (Node module : webModules)
      {
         String webUri = module.textValue("web-uri");
         String contextRoot = module.textValue("context-root");

         if (webUri != null || contextRoot != null)
         {
            result.add(new WebModuleImpl(webUri, contextRoot));
         }
      }
      return result;
   }

   @Override
   public List<String> getEjbModules()
   {
      return model.textValues("module/ejb");
   }

   @Override
   public List<String> getJavaModules()
   {
      return model.textValues("module/java");
   }

   @Override
   public List<String> getConnectorModules()
   {
      return model.textValues("module/connector");
   }

   @Override
   public List<SecurityRole> getSecurityRoles()
   {
      List<SecurityRole> result = new ArrayList<SecurityRole>();

      List<Node> securityRoles = model.get("security-role");
      for (Node module : securityRoles)
      {
         String name = module.textValue("role-name");
         String desc = module.textValue("description");

         if (name != null || desc != null)
         {
            result.add(new SecurityRoleImpl(name, desc));
         }
      }
      return result;

   }
}
