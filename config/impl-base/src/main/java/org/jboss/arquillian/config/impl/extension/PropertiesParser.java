/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.config.impl.extension;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;

/**
 * Add/Override arquillian.xml based on SystemProperties.
 *
 * arq.container.[qualifier].mode
 * arq.container.[qualifier].default
 * arq.container.[qualifier].configuration.[property_name]
 * arq.container.[qualifier].protocol.[type].[property_name]
 *
 * arq.group.[qualifier].default
 * arq.group.[qualifier].container.[qualifier].configuration.[property_name]
 * arq.group.[qualifier].container.[qualifier].protocol.[type].[property_name]
 *
 * arq.extension.[qualifier].[property_name]
 *
 * arq.defaultprotocol.[type]
 * arq.defaultprotocol.[type].[property_name]
 *
 * arq.engine.[property_name]
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class PropertiesParser
{
   private static String ARQ_PROPERTY                       = "arq\\..*";

   private static String ARQ_ENGINE_PROPERTY                = "arq\\.engine\\.(.*)";

   private static String ARQ_CONTAINER                      = "arq\\.container\\.(.*)\\.(.*)";
   private static String ARQ_CONTAINER_CONFIGURATION        = "arq\\.container\\.(.*)\\.configuration\\.(.*)";
   private static String ARQ_CONTAINER_PROTOCOL             = "arq\\.container\\.(.*)\\.protocol\\.(.*)\\.(.*)";

   private static String ARQ_GROUP                          = "arq\\.group\\.(.*)\\.(.*)";
   private static String ARQ_GROUP_CONTAINER                = "arq\\.group\\.(.*)\\.container\\.(.*)\\.(.*)";
   private static String ARQ_GROUP_CONTAINER_CONFIGURATION  = "arq\\.group\\.(.*)\\.container\\.(.*)\\.configuration\\.(.*)";
   private static String ARQ_GROUP_CONTAINER_PROTOCOL       = "arq\\.group\\.(.*)\\.container\\.(.*)\\.protocol\\.(.*)\\.(.*)";

   private static String ARQ_DEFAULT_PROTOCOL               = "arq\\.defaultprotocol\\.(.*)\\.(.*)";
   private static String ARQ_EXTENSION                      = "arq\\.extension\\.(.*)\\.(.*)";


   private Handler[] handlers = new Handler[]{
         new EngineProperty(ARQ_ENGINE_PROPERTY),
         new ContainerConfiguration(ARQ_CONTAINER_CONFIGURATION),
         new ContainerProtocol(ARQ_CONTAINER_PROTOCOL),
         new Container(ARQ_CONTAINER),
         new Extension(ARQ_EXTENSION),
         new GroupContainerConfiguration(ARQ_GROUP_CONTAINER_CONFIGURATION),
         new GroupContainerProtocol(ARQ_GROUP_CONTAINER_PROTOCOL),
         new GroupContainer(ARQ_GROUP_CONTAINER),
         new Group(ARQ_GROUP),
         new DefaultProtocol(ARQ_DEFAULT_PROTOCOL)
   };

   public void addProperties(ArquillianDescriptor descriptor, Properties properties)
   {
      if(descriptor == null)
      {
         throw new IllegalArgumentException("Descriptor must be specified");
      }
      if(properties == null)
      {
         throw new IllegalArgumentException("Properties must be specified");
      }

      Set<Entry<Object, Object>> filteredProps = filterProperties(properties);
      for(Entry<Object, Object> entry : filteredProps)
      {
         for(Handler handler : handlers)
         {
            if(handler.handle(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()), descriptor))
            {
               break;
            }
         }
      }
   }

   private Set<Entry<Object, Object>> filterProperties(Properties properties)
   {
      Set<Entry<Object, Object>> filtered = new HashSet<Entry<Object,Object>>();
      for(Entry<Object, Object> entry : properties.entrySet())
      {
         if(String.valueOf(entry.getKey()).matches(ARQ_PROPERTY))
         {
            filtered.add(entry);
         }
      }
      return filtered;
   }


   /*******************************
    *
    * Handlers
    *
    *******************************/

   private class DefaultProtocol extends Handler
   {
      public DefaultProtocol(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String typeName = matcher.group(1);
         String propertyName = matcher.group(2);

         descriptor.defaultProtocol(typeName).property(propertyName, value);
      }
   }

   private class Group extends Handler
   {
      public Group(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String groupName = matcher.group(1);
         String attributeName = matcher.group(2);

         if("default".equals(attributeName))
         {
            descriptor.group(groupName).setGroupDefault();
         }
         else
         {
            throw new RuntimeException("Unknown arquillian container attribute[" + attributeName + "] with value[" + value + "]");
         }
      }
   }

   private class GroupContainer extends Handler
   {
      public GroupContainer(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String groupName = matcher.group(1);
         String containerName = matcher.group(2);
         String attributeName = matcher.group(3);

         if("mode".equals(attributeName))
         {
            descriptor.group(groupName).container(containerName).setMode(value);
         }
         else if("default".equals(attributeName))
         {
            descriptor.group(groupName).container(containerName).setDefault();
         }
         else
         {
            throw new RuntimeException("Unknown arquillian container attribute[" + attributeName + "] with value[" + value + "]");
         }
      }
   }

   private class GroupContainerProtocol extends Handler
   {
      public GroupContainerProtocol(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String groupName = matcher.group(1);
         String containerName = matcher.group(2);
         String protocolName = matcher.group(3);
         String propertyName = matcher.group(4);

         descriptor.group(groupName).container(containerName).protocol(protocolName).property(propertyName, value);
      }
   }

   private class GroupContainerConfiguration extends Handler
   {
      public GroupContainerConfiguration(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String groupName = matcher.group(1);
         String containerName = matcher.group(2);
         String propertyName = matcher.group(3);

         descriptor.group(groupName).container(containerName).property(propertyName, value);
      }
   }

   private class Extension extends Handler
   {
      public Extension(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String extensionName = matcher.group(1);
         String propertyName = matcher.group(2);

         descriptor.extension(extensionName).property(propertyName, value);
      }
   }

   private class Container extends Handler
   {
      public Container(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String containerName = matcher.group(1);
         String attributeName = matcher.group(2);

         if("mode".equals(attributeName))
         {
            descriptor.container(containerName).setMode(value);
         }
         else if("default".equals(attributeName))
         {
            descriptor.container(containerName).setDefault();
         }
         else
         {
            throw new RuntimeException("Unknown arquillian container attribute[" + attributeName + "] with value[" + value + "]");
         }
      }
   }

   private class ContainerProtocol extends Handler
   {
      public ContainerProtocol(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String containerName = matcher.group(1);
         String protocolName = matcher.group(2);
         String propertyName = matcher.group(3);

         descriptor.container(containerName).protocol(protocolName).property(propertyName, value);
      }
   }

   private class ContainerConfiguration extends Handler
   {
      public ContainerConfiguration(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String containerName = matcher.group(1);
         String propertyName = matcher.group(2);

         descriptor.container(containerName).property(propertyName, value);
      }
   }

   private class EngineProperty extends Handler
   {
      public EngineProperty(String expression)
      {
         super(expression);
      }

      @Override
      protected void handle(Matcher matcher, String value, ArquillianDescriptor descriptor)
      {
         String propertyName = matcher.group(1);
         if("deploymentExportPath".equals(propertyName))
         {
            descriptor.engine().deploymentExportPath(value);
         }
         else if("maxTestClassesBeforeRestart".equals(propertyName))
         {
            descriptor.engine().maxTestClassesBeforeRestart(Integer.parseInt(value));
         }
         else
         {
            throw new RuntimeException("Unknown arquillian engine property[" + propertyName + "] with value[" + value + "]");
         }
      }
   }

   private abstract class Handler
   {
      private Pattern expression;

      public Handler(String expression)
      {
         this.expression = Pattern.compile(expression);
      }

      public boolean handle(String propertyName, String value, ArquillianDescriptor descriptor)
      {
         Matcher matcher = expression.matcher(propertyName);
         if(matcher.matches())
         {
            handle(matcher, value, descriptor);
            return true;
         }
         return false;
      }

      protected abstract void handle(Matcher matcher, String value, ArquillianDescriptor descriptor);
   }
}
