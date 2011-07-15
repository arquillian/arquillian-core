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

import java.util.List;

import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.ServletMappingDef;
import org.jboss.shrinkwrap.descriptor.spi.Node;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class ServletMappingDefImpl extends ServletDefImpl implements ServletMappingDef
{
   private final Node mapping;

   public ServletMappingDefImpl(String descriptorName, Node webApp, Node servletNode, Node mappingNode)
   {
      super(descriptorName, webApp, servletNode);
      this.mapping = mappingNode;
   }

   @Override
   public String getServletName()
   {
      return mapping.getTextValueForPatternName("servlet-name");
   }

   @Override
   public ServletMappingDef servletName(String servletName)
   {
      mapping.getOrCreate("servlet-name").text(servletName);
      return this;
   }

   @Override
   public List<String> getUrlPatterns()
   {
      return mapping.getTextValuesForPatternName("url-pattern");
   }

   @Override
   public ServletMappingDef urlPattern(String urlPattern)
   {
      mapping.createChild("url-pattern").text(urlPattern);
      return this;
   }

   @Override
   public ServletMappingDef urlPatterns(String... urlPatterns)
   {
      for (String string : urlPatterns)
      {
         urlPattern(string);
      }
      return this;
   }
}
