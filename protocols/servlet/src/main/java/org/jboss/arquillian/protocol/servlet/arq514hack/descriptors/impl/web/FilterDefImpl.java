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
package org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.impl.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.FilterDef;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.FilterMappingDef;
import org.jboss.shrinkwrap.descriptor.spi.Node;

/**
 * Backend object view of a Web Descriptor Filter element
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class FilterDefImpl extends WebAppDescriptorImpl implements FilterDef
{
   private final Node filter;

   public FilterDefImpl(String descriptorName, final Node webApp, final Node filter)
   {
      super(descriptorName, webApp);
      this.filter = filter;
   }

   @Override
   public String getName()
   {
      return filter.textValue("filter-name");
   }

   @Override
   public FilterDef name(String name)
   {
      filter.getOrCreate("filter-name").text(name);
      return this;
   }

   @Override
   public FilterDef asyncSupported(boolean value)
   {
      filter.getOrCreate("async-supported").text(value);
      return this;
   }

   @Override
   public boolean isAsyncSupported()
   {
      return Strings.isTrue(filter.textValue("async-supported"));
   }

   @Override
   public String getFilterClass()
   {
      return filter.textValue("filter-class");
   }

   @Override
   public FilterDef filterClass(String clazz)
   {
      filter.getOrCreate("filter-class").text(clazz);
      return this;
   }

   @Override
   public FilterDef initParam(String name, Object value)
   {
      InitParamDefImpl param = new InitParamDefImpl(getDescriptorName(), getRootNode(), filter);
      param.initParam(name, value == null ? null : value.toString());
      return this;
   }

   @Override
   public String getInitParam(String name)
   {
      Map<String, String> params = getInitParams();
      for (Entry<String, String> e : params.entrySet())
      {
         if (e.getKey() != null && e.getKey().equals(name))
         {
            return e.getValue();
         }
      }
      return null;
   }

   @Override
   public Map<String, String> getInitParams()
   {
      Map<String, String> result = new HashMap<String, String>();
      List<Node> params = filter.get("init-param");
      for (Node node : params)
      {
         result.put(node.textValue("param-name"), node.textValue("param-value"));
      }
      return result;
   }

   @Override
   public FilterMappingDef mapping()
   {
      Node mappingNode = getRootNode().create("filter-mapping");
      FilterMappingDefImpl mapping = new FilterMappingDefImpl(getDescriptorName(), getRootNode(), filter, mappingNode);
      mapping.filterName(getName());
      return mapping;
   }

   @Override
   public List<FilterMappingDef> getMappings()
   {
      List<FilterMappingDef> result = new ArrayList<FilterMappingDef>();
      List<FilterMappingDef> mappings = getFilterMappings();
      for (FilterMappingDef mapping : mappings)
      {
         if (Strings.areEqualTrimmed(this.getName(), mapping.getFilterName()))
         {
            result.add(mapping);
         }
      }
      return result;
   }

   public Node getNode()
   {
      return filter;
   }

}
