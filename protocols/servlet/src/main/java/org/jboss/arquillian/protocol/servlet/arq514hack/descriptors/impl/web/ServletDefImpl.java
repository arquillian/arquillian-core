/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.impl.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.ServletDef;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.ServletMappingDef;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class ServletDefImpl extends WebAppDescriptorImpl implements ServletDef
{
   private final Node servlet;

   public ServletDefImpl(String descriptorName, Node webApp, Node servlet)
   {
      super(descriptorName, webApp);
      this.servlet = servlet;
   }

   @Override
   public ServletDef name(String name)
   {
      servlet.getOrCreate("servlet-name").text(name);
      return this;
   }

   @Override
   public ServletDef asyncSupported(boolean value)
   {
      servlet.getOrCreate("async-supported").text(value);
      return this;
   }

   @Override
   public ServletDef initParam(String name, Object value)
   {
      InitParamDefImpl param = new InitParamDefImpl(getDescriptorName(), getRootNode(), servlet);
      param.initParam(name, value == null ? null : value.toString());
      return this;
   }

   @Override
   public ServletDef loadOnStartup(int order)
   {
      servlet.getOrCreate("load-on-startup").text(order);
      return this;
   }

   @Override
   public ServletMappingDef mapping()
   {
      Node mappingNode = getRootNode().createChild("servlet-mapping");
      ServletMappingDef mapping = new ServletMappingDefImpl(getDescriptorName(), getRootNode(), servlet, mappingNode);
      mapping.servletName(getName());
      return mapping;
   }

   @Override
   public ServletDef servletClass(Class<?> clazz)
   {
      return servletClass(clazz.getName());
   }

   @Override
   public ServletDef servletClass(String clazz)
   {
      servlet.getOrCreate("servlet-class").text(clazz);
      return this;
   }

   @Override
   public String getServletClass()
   {
      if (servlet.getSingle("servlet-class") != null)
      {
         return servlet.getSingle("servlet-class").getText();
      }
      return null;
   }

   @Override
   public String getName()
   {
      return servlet.getTextValueForPatternName("servlet-name");
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
      List<Node> params = servlet.get("init-param");
      for (Node node : params)
      {
         result.put(node.getTextValueForPatternName("param-name"), node.getTextValueForPatternName("param-value"));
      }
      return result;
   }

   @Override
   public boolean isAsyncSupported()
   {
      return Strings.isTrue(servlet.getTextValueForPatternName("async-supported"));
   }

   @Override
   public int getLoadOnStartup() throws NumberFormatException
   {
      String tex = servlet.getTextValueForPatternName("load-on-startup");
      return tex == null ? null : Integer.valueOf(tex);
   }

   public Node getNode()
   {
      return servlet;
   }

}
