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
package org.jboss.arquillian.impl.configuration.api;

import static org.jboss.arquillian.impl.configuration.api.AssertXPath.assertXPath;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Test;


/**
 * ArquillianDescriptorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianDescriptorTestCase
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
   private static final String PROPERTY_NAME_1 = "test-name";
   private static final String PROPERTY_VALUE_1 = "test-value";
   private static final String PROPERTY_NAME_2 = "test2-name";
   private static final String PROPERTY_VALUE_2 = "test2-value";
   private static final String PROPERTY_NAME_3 = "test3-name";
   private static final String PROPERTY_VALUE_3 = "test3-value";
   
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
   public void shouldBeAbleToAddContainer() throws Exception
   {
      desc = create()
            .container(CONTAINER_NAME_1).setDefault()
            .container(CONTAINER_NAME_2).exportAsString();
      
      assertXPath(desc, "/arquillian/container/@qualifier", CONTAINER_NAME_1, CONTAINER_NAME_2);
      assertXPath(desc, "/arquillian/container[1]/@default", "true");
   }

   @Test
   public void shouldBeAbleToAddDefaultProtocol() throws Exception
   {
      desc = create()
            .defaultProtocol(PROTOCOL_TYPE_1)
               .property(PROPERTY_NAME_1, PROPERTY_VALUE_1)
            .exportAsString();
      
      assertXPath(desc, "/arquillian/protocol/@type", PROTOCOL_TYPE_1);
      assertXPath(desc, "/arquillian/protocol/configuration/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/protocol/configuration/property/text()", PROPERTY_VALUE_1);
   }

   @Test
   public void shouldBeAbleToAddContainerWithDependencies() throws Exception
   {
      desc = create()
            .container(CONTAINER_NAME_1)
            .dependency(DEPENDENCY_1)
            .dependency(DEPENDENCY_2).exportAsString();
      
      assertXPath(desc, "/arquillian/container/dependencies/dependency", DEPENDENCY_1, DEPENDENCY_2);
   }

   @Test
   public void shouldBeAbleToAddContainerWithMultipleProtocols() throws Exception
   {
      desc = create()
            .container(CONTAINER_NAME_1)
               .protocol(PROTOCOL_TYPE_1)
                  .property(PROPERTY_NAME_1, PROPERTY_VALUE_1)
               .protocol(PROTOCOL_TYPE_2)
                  .property(PROPERTY_NAME_2, PROPERTY_VALUE_2)
            .exportAsString();
      
      assertXPath(desc, "/arquillian/container/@qualifier", CONTAINER_NAME_1);
      assertXPath(desc, "/arquillian/container/protocol[1]/@type", PROTOCOL_TYPE_1);
      assertXPath(desc, "/arquillian/container/protocol[1]/configuration/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/container/protocol[1]/configuration/property/text()", PROPERTY_VALUE_1);

      assertXPath(desc, "/arquillian/container/protocol[2]/@type", PROTOCOL_TYPE_2);
      assertXPath(desc, "/arquillian/container/protocol[2]/configuration/property/@name", PROPERTY_NAME_2);
      assertXPath(desc, "/arquillian/container/protocol[2]/configuration/property/text()", PROPERTY_VALUE_2);
   }
   
   @Test
   public void shouldBeAbleToAddContainerWithConfiguration() throws Exception
   {
      desc = create()
            .container(CONTAINER_NAME_1)
               .property(PROPERTY_NAME_1, PROPERTY_VALUE_1)
            .container(CONTAINER_NAME_2)
               .property(PROPERTY_NAME_2, PROPERTY_VALUE_2)
            .exportAsString();
      
      assertXPath(desc, "/arquillian/container[1]/@qualifier", CONTAINER_NAME_1);
      
      assertXPath(desc, "/arquillian/container[1]/configuration/property/@name", PROPERTY_NAME_1);
      assertXPath(desc, "/arquillian/container[1]/configuration/property/text()", PROPERTY_VALUE_1);

      assertXPath(desc, "/arquillian/container[2]/@qualifier", CONTAINER_NAME_2);
      assertXPath(desc, "/arquillian/container[2]/configuration/property/@name", PROPERTY_NAME_2);
      assertXPath(desc, "/arquillian/container[2]/configuration/property/text()", PROPERTY_VALUE_2);
   }
   
   
   @Test
   public void shouldBeAbleToAddGroupWithContainer() throws Exception
   {
      desc = create()
            .group(GROUP_NAME_1)
               .container(CONTAINER_NAME_1)
               .container(CONTAINER_NAME_2)
            .group(GROUP_NAME_2)
               .container(CONTAINER_NAME_3).exportAsString();
      
      assertXPath(desc, "/arquillian/group/@qualifier", GROUP_NAME_1, GROUP_NAME_2);
      assertXPath(desc, "/arquillian/group/container/@qualifier", CONTAINER_NAME_1, CONTAINER_NAME_2, CONTAINER_NAME_3);
   }
   
   @Test
   public void shouldBeAbleToAddEverything() throws Exception
   {
      desc = create()
            .defaultProtocol(PROTOCOL_TYPE_1)
               .property(PROPERTY_VALUE_3, PROPERTY_VALUE_3)
            .container(CONTAINER_NAME_1)
               .property(PROPERTY_NAME_1, PROPERTY_VALUE_1)
               .dependency(DEPENDENCY_1)
               .protocol(PROTOCOL_TYPE_1)
                  .property(PROPERTY_NAME_2, PROPERTY_VALUE_2)
            .group(GROUP_NAME_1)
               .container(CONTAINER_NAME_2)
                  .property(PROPERTY_NAME_1, PROPERTY_VALUE_1)
                  .dependency(DEPENDENCY_2)
                  .protocol(PROTOCOL_TYPE_2)
                     .property(PROPERTY_NAME_3, PROPERTY_VALUE_3)
            .group(GROUP_NAME_2)
               .container(CONTAINER_NAME_3)
                  .protocol(PROTOCOL_TYPE_3)
                     .property(PROPERTY_NAME_1, PROPERTY_VALUE_1)
               .container(CONTAINER_NAME_4)
            .exportAsString();
            
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper --------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private ArquillianDescriptor create()
   {
      return Descriptors.create(ArquillianDescriptor.class);
   }
}
