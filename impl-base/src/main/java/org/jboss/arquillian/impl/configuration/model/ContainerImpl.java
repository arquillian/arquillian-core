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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * ContainerImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "container")
@XmlType(name = "", propOrder = {
      "properties",
      "protocols",
      "dependencies"
})
public class ContainerImpl 
{
   @XmlAttribute(name ="qualifier")
   private String name;
   
   @XmlAttribute(name ="default")
   private boolean isDefault;
   
   @XmlElement(name = "protocol")
   private List<ProtocolImpl> protocols;

   @XmlElementWrapper(name = "dependencies")
   @XmlElement(name = "dependency")
   private List<String> dependencies;
   
   @XmlElementWrapper(name = "configuration")
   @XmlElement(name = "property")
   private Set<PropertyImpl> properties;

   public ContainerImpl()
   {
   }
   
   public ContainerImpl(String name)
   {
      this.name = name;
   }
   
   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public ContainerImpl setName(String name)
   {
      this.name = name;
      return this;
   }
   
   /**
    * @param isDefault the isDefault to set
    */
   public ContainerImpl setDefault(boolean isDefault)
   {
      this.isDefault = isDefault;
      return this;
   }
   
   /**
    * @return the isDefault
    */
   public boolean isDefault()
   {
      return isDefault;
   }
   
   /**
    * @return the dependecies
    */
   public List<String> getDependencies()
   {
      if(dependencies == null)
      {
         dependencies = new ArrayList<String>();
      }
      return dependencies;
   }
   
   public ContainerImpl addDependency(String value)
   {
      getDependencies().add(value);
      return this;
   }
   
   /**
    * @return the protocols
    */
   public List<ProtocolImpl> getProtocols()
   {
      if(protocols == null)
      {
         protocols = new ArrayList<ProtocolImpl>();
      }
      return protocols;
   }
   
   /**
    * @return the properties
    */
   public Set<PropertyImpl> getProperties()
   {
      if(properties == null)
      {
         properties = new HashSet<PropertyImpl>();
      }
      return properties;
   }
   
   public ContainerImpl addProperty(String name, String value)
   {
      getProperties().add(new PropertyImpl(name, value));
      return this;
   }
}
