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

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.DefaultProtocolDef;
import org.jboss.arquillian.config.descriptor.api.EngineDef;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.config.descriptor.api.GroupDef;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.jboss.shrinkwrap.descriptor.spi.node.NodeDescriptorImplBase;

/**
 * ArquillianDescriptor
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianDescriptorImpl extends NodeDescriptorImplBase implements ArquillianDescriptor
{
   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private Node model;
   
   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public ArquillianDescriptorImpl(String descirptorName)
   {
      this(descirptorName, new Node("arquillian")
         .attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
         .attribute("xmlns", "http://jboss.org/schema/arquillian")
         .attribute("xsi:schemaLocation", "http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd"));
   }
   
   public ArquillianDescriptorImpl(String descirptorName, Node model)
   {
      super(descirptorName);
      this.model = model;
   }

   //-------------------------------------------------------------------------------------||
   // API --------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#defaultProtocol()
    */
   @Override
   public DefaultProtocolDef defaultProtocol(String type)
   {
      return new DefaultProtocolDefImpl(getDescriptorName(), model, model.getOrCreate("defaultProtocol")).setType(type);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#getDefaultProtocol()
    */
   @Override
   public DefaultProtocolDef getDefaultProtocol()
   {
      if(model.getSingle("defaultProtocol") != null)
      {
         return new DefaultProtocolDefImpl(getDescriptorName(), model, model.getSingle("defaultProtocol"));
      }
      return null;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#engine()
    */
   @Override
   public EngineDef engine()
   {
      return new EngineDefImpl(getDescriptorName(), model, model.getOrCreate("engine"));
   }
   
   public ContainerDef container(String name) 
   {
      return new ContainerDefImpl(getDescriptorName(), model, model.getOrCreate("container@qualifier=" + name));
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#group(java.lang.String)
    */
   @Override
   public GroupDef group(String name)
   {
      return new GroupDefImpl(getDescriptorName(), model, model.getOrCreate("group@qualifier=" + name));
   }
   
   @Override
   public ExtensionDef extension(String name)
   {
      return new ExtensionDefImpl(getDescriptorName(), model, model.getOrCreate("extension@qualifier=" + name));
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#getContainers()
    */
   @Override
   public List<ContainerDef> getContainers()
   {
      List<ContainerDef> containers = new ArrayList<ContainerDef>();
      for(Node container : model.get("container"))
      {
         containers.add(new ContainerDefImpl(getDescriptorName(), model, container));
      }
      return containers;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#getGroups()
    */
   @Override
   public List<GroupDef> getGroups()
   {
      List<GroupDef> groups = new ArrayList<GroupDef>();
      for(Node group : model.get("group"))
      {
         groups.add(new GroupDefImpl(getDescriptorName(), model, group));
      }
      return groups;
   }
   
   @Override
   public List<ExtensionDef> getExtensions()
   {
      List<ExtensionDef> extensions = new ArrayList<ExtensionDef>();
      for(Node extension: model.get("extension"))
      {
         extensions.add(new ExtensionDefImpl(getDescriptorName(), model, extension));
      }
      return extensions;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - SchemaDescriptorProvider --------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.shrinkwrap.descriptor.spi.node.NodeDescriptor#getRootNode()
    */
   @Override
   public Node getRootNode()
   {
      return model;
   }

	@Override
	public ArquillianDescriptor resolve() 
	{
		String descrStr = this.exportAsString();
		ArquillianDescriptor newArqDescriptor = 
			Descriptors.importAs(ArquillianDescriptor.class)
				.from(StringPropertyReplacer.replaceProperties(descrStr));
		
		return newArqDescriptor;
	}   
   
}
