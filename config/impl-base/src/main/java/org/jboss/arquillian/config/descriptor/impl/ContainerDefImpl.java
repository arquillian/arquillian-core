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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.ProtocolDef;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * ContainerDefImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerDefImpl extends ArquillianDescriptorImpl implements ContainerDef
{
   private Node container;
   
   // test only
   public ContainerDefImpl(String descirptorName)
   {
      this(descirptorName, new Node("arquillian"));
   }

   // test only
   public ContainerDefImpl(String descirptorName, Node model)
   {
      this(descirptorName, model, model.createChild("container"));
   }
   
   public ContainerDefImpl(String descirptorName, Node model, Node container)
   {
      super(descirptorName, model);
      this.container = container;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - ContainerDescriptor -------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#setContainerName(java.lang.String)
    */
   public ContainerDef setContainerName(String name)
   {
      container.attribute("qualifier", name);
      return this;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#getContainerName()
    */
   @Override
   public String getContainerName()
   {
      return container.getAttribute("qualifier");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#setDefault()
    */
   @Override
   public ContainerDef setDefault()
   {
      container.attribute("default", true);
      return this;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#isDefault()
    */
   @Override
   public boolean isDefault()
   {
      return Boolean.parseBoolean(container.getAttribute("default"));
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#setMode(java.lang.String)
    */
   @Override
   public ContainerDef setMode(String mode)
   {
      container.attribute("mode", mode);
      return this;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#getMode()
    */
   @Override
   public String getMode()
   {
      return container.getAttribute("mode") == null ? "suite" : container.getAttribute("mode");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDescription#dependency(java.lang.String)
    */
   @Override
   public ContainerDef dependency(String artifactId)
   {
      container.getOrCreate("dependencies").getOrCreate("dependency=" + artifactId);
      return this;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDescription#protocol()
    */
   @Override
   public ProtocolDef protocol(String type)
   {
      return new ProtocolDefImpl(
            getDescriptorName(), 
            getRootNode(), 
            container, 
            container.getOrCreate("protocol@type=" + type));
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDescription#property(java.lang.String, java.lang.String)
    */
   @Override
   public ContainerDef property(String name, String value)
   {
      container.getOrCreate("configuration").getOrCreate("property@name=" + name).text(value);
      return this;
   }
 
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDescription#overrideProperty(java.lang.String, java.lang.String)
    */
   @Override
   public ContainerDef overrideProperty(String name, String value) 
   {
      container.getOrCreate("configuration").removeChild("property@name=" + name);
      container.getOrCreate("configuration").getOrCreate("property@name=" + name).text(value);
      return this;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#getProperties()
    */
   @Override
   public Map<String, String> getContainerProperties()
   {
      Node props = container.getSingle("configuration");
      Map<String, String> properties = new HashMap<String, String>();
      
      if(props != null)
      {
         for(Node prop: props.get("property"))
         {
            properties.put(prop.getAttribute("name"), prop.getText());
         }
      }
      return properties;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#getProtcols()
    */
   @Override
   public List<ProtocolDef> getProtocols()
   {
      List<ProtocolDef> protocols = new ArrayList<ProtocolDef>();
      for(Node proto : container.get("protocol"))
      {
         protocols.add(new ProtocolDefImpl(getDescriptorName(), getRootNode(), container, proto));
      }
      return protocols;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ContainerDef#getDependencies()
    */
   @Override
   public List<String> getDependencies()
   {
      List<String> dependencies = new ArrayList<String>();
      if(container.getSingle("dependencies") != null)
      {
         for(Node dep : container.getSingle("dependencies").get("dependency"))
         {
            dependencies.add(dep.getText());
         }
      }
      return dependencies;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return container.toString(true);
   }
}
