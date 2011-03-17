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
package org.jboss.arquillian.impl.configuration;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.impl.configuration.api.ExtensionDef;
import org.jboss.shrinkwrap.descriptor.api.Node;

/**
 * ExtensionDefImpl
 * 
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ExtensionDefImpl extends ArquillianDescriptorImpl implements ExtensionDef
{
   private Node extension;

   // test only
   public ExtensionDefImpl(String descirptorName)
   {
      this(descirptorName, new Node("arquillian"));
   }

   // test only
   public ExtensionDefImpl(String descirptorName, Node model)
   {
      this(descirptorName, model, model.create("extension"));
   }

   public ExtensionDefImpl(String descirptorName, Node model, Node extension)
   {
      super(descirptorName, model);
      this.extension = extension;
   }

   // -------------------------------------------------------------------------------------||
   // Required Implementations - ExtensionDef   -------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ExtensionDef#property(java.lang.String, java.lang.String)
    */
   @Override
   public ExtensionDef property(String name, String value)
   {
      extension.getOrCreate("configuration").create("property").attribute("name", name).text(value);
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.impl.configuration.api.ExtensionDef#getExtensionProperties()
    */
   @Override
   public Map<String, String> getExtensionProperties()
   {

      Node props = extension.getSingle("configuration");
      Map<String, String> properties = new HashMap<String, String>();

      if (props != null)
      {
         for (Node prop : props.get("property"))
         {
            properties.put(prop.attribute("name"), prop.text());
         }
      }
      return properties;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.impl.configuration.api.ExtensionDef#getExtensionName
    * ()
    */
   @Override
   public String getExtensionName()
   {
      return extension.attribute("qualifier");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ExtensionDef#setExtensionName(java.lang.String)
    */
   @Override
   public ExtensionDef setExtensionName(String name)
   {
      extension.attribute("qualifier", name);
      return this;    
   }

}