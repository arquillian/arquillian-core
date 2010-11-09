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
package org.jboss.arquillian.impl.configuration.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;

import org.jboss.arquillian.impl.configuration.ArquillianDescriptorImpl;
import org.jboss.shrinkwrap.descriptor.spi.SchemaModel;

/**
 * ArquillianModel
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "arquillian")
@XmlType(name = "", propOrder = {
      "protocol",
      "containers",
      "groups"
})
public class ArquillianModel implements SchemaModel
{
   @XmlElement(name = "protocol")
   private ProtocolImpl protocol;

   @XmlElement(name = "container")
   private List<ContainerImpl> containers;
   
   @XmlElement(name = "group")
   private List<GroupImpl> groups;
   
   /**
    * @return the containers
    */
   public List<ContainerImpl> getContainers()
   {
      if(containers == null)
      {
         containers = new ArrayList<ContainerImpl>();
      }
      return containers;
   }
   
   /**
    * @return the groups
    */
   public List<GroupImpl> getGroups()
   {
      if(groups == null)
      {
         groups = new ArrayList<GroupImpl>();
      }
      return groups;
   }
   
   /**
    * @return the protocol
    */
   public ProtocolImpl getProtocol()
   {
      return protocol;
   }
   
   /**
    * @param protocol the protocol to set
    */
   public void setProtocol(ProtocolImpl protocol)
   {
      this.protocol = protocol;
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - SchemaModel ---------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.shrinkwrap.descriptor.spi.SchemaModel#getVersion()
    */
   @Override
   public String getVersion()
   {
      return "1.0";
   }

   /* (non-Javadoc)
    * @see org.jboss.shrinkwrap.descriptor.spi.SchemaModel#getNamespace()
    */
   @Override
   public String getNamespace()
   {
      return ArquillianDescriptorImpl.class.getPackage().getAnnotation(XmlSchema.class).namespace();
   }

   /* (non-Javadoc)
    * @see org.jboss.shrinkwrap.descriptor.spi.SchemaModel#getSchemaLocation()
    */
   @Override
   public String getSchemaLocation()
   {
      final String namespace = getNamespace();
      return new StringBuilder().append(namespace).append(" ").append(namespace).append("/arquillian_")
            .append(getVersion().replace('.', '_')).append(".xsd").toString();
   }

}
