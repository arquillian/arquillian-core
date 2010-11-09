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

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.configuration.api.ContainerDef;
import org.jboss.arquillian.impl.configuration.api.DefaultProtocolDef;
import org.jboss.arquillian.impl.configuration.api.GroupDef;
import org.jboss.arquillian.impl.configuration.model.ArquillianModel;
import org.jboss.arquillian.impl.configuration.model.ContainerImpl;
import org.jboss.arquillian.impl.configuration.model.GroupImpl;
import org.jboss.arquillian.impl.configuration.model.ProtocolImpl;
import org.jboss.shrinkwrap.descriptor.impl.base.SchemaDescriptorImplBase;

/**
 * ArquillianDescriptor
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianDescriptorImpl extends SchemaDescriptorImplBase<ArquillianModel> implements ArquillianDescriptor
{
   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private ArquillianModel model;
   
   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public ArquillianDescriptorImpl()
   {
      this(new ArquillianModel());
   }
   
   public ArquillianDescriptorImpl(ArquillianModel model)
   {
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
      ProtocolImpl protocol = new ProtocolImpl(type);
      model.setProtocol(protocol);
      return new DefaultProtocolDefImpl(model, protocol);
   }
   
   public ContainerDef container(String name) 
   {
      ContainerImpl container = new ContainerImpl(name); 
      model.getContainers().add(container);
      
      return new ContainerDefImpl(model, container);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor#group(java.lang.String)
    */
   @Override
   public GroupDef group(String name)
   {
      GroupImpl group = new GroupImpl(name);
      model.getGroups().add(group);
      return new GroupDefImpl(getSchemaModel(), group);
   }
   
   //-------------------------------------------------------------------------------------||
   // Required Implementations - SchemaDescriptorProvider --------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.shrinkwrap.descriptor.spi.SchemaDescriptorProvider#getSchemaModel()
    */
   @Override
   public ArquillianModel getSchemaModel()
   {
      return model;
   }
   
}
