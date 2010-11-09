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

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * ProtocolImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "protocol")
public class ProtocolImpl
{
   @XmlAttribute(name = "type")
   private String type;
   
   @XmlElementWrapper(name = "configuration")
   @XmlElement(name = "property")
   private Set<PropertyImpl> properties;

   public ProtocolImpl()
   {
   }

   public ProtocolImpl(String type)
   {
      this.type = type;
   }
   
   /**
    * @param type the type to set
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * @return the type
    */
   public String getType()
   {
      return type;
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
}
