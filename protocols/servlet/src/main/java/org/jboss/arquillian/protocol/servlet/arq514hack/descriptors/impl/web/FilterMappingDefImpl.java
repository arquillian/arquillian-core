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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.DispatcherType;

import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.FilterMappingDef;
import org.jboss.shrinkwrap.descriptor.spi.Node;

/**
 * Backend object view of a Web Descriptor Filter Mapping element
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class FilterMappingDefImpl extends FilterDefImpl implements FilterMappingDef
{
   private final Node mapping;

   public FilterMappingDefImpl(String descriptorName, Node rootNode, Node filter, Node mapping)
   {
      super(descriptorName, rootNode, filter);
      this.mapping = mapping;
   }

   @Override
   public String getFilterName()
   {
      return mapping.textValue("filter-name");
   }

   @Override
   public FilterMappingDef filterName(String filterName)
   {
      mapping.getOrCreate("filter-name").text(filterName);
      return this;
   }

   @Override
   public List<String> getUrlPatterns()
   {
      return mapping.textValues("url-pattern");
   }

   @Override
   public FilterMappingDef urlPattern(String urlPattern)
   {
      mapping.create("url-pattern").text(urlPattern);
      return this;
   }

   @Override
   public FilterMappingDef urlPatterns(String... urlPatterns)
   {
      for (String string : urlPatterns)
      {
         urlPattern(string);
      }
      return this;
   }

   @Override
   public FilterMappingDef dispatchType(DispatcherType type)
   {
      if (!getDispatchTypes().contains(type))
      {
         mapping.create("dispatcher").text(type.name());
      }
      return this;
   }

   @Override
   public FilterMappingDef dispatchTypes(DispatcherType... types)
   {
      for (DispatcherType dispatcherType : types)
      {
         dispatchType(dispatcherType);
      }
      return this;
   }

   @Override
   public Set<DispatcherType> getDispatchTypes()
   {
      List<String> values = mapping.textValues("dispatcher");
      Set<DispatcherType> result = new HashSet<DispatcherType>();
      for (String string : values)
      {
         result.add(DispatcherType.valueOf(string));
      }
      return result;
   }

   @Override
   public List<String> getServletNames()
   {
      return mapping.textValues("servlet-name");
   }

   @Override
   public FilterMappingDef servletName(String name)
   {
      if (!getServletMappings().contains(name))
      {
         mapping.create("servlet-name").text(name);
      }
      return this;
   }

   @Override
   public FilterMappingDef servletNames(String... names)
   {
      for (String string : names)
      {
         servletName(string);
      }
      return this;
   }

}
