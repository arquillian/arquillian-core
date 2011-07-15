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

import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.CookieConfigDef;
import org.jboss.shrinkwrap.descriptor.spi.Node;

/**
 * @author Dan Allen
 */
public class CookieConfigDefImpl extends WebAppDescriptorImpl implements CookieConfigDef
{
   public CookieConfigDefImpl(String descriptorName, Node webApp)
   {
      super(descriptorName, webApp);
   }
   
   private Node cookieConfig()
   {
      return getRootNode().getOrCreate("session-config").getOrCreate("cookie-config");
   }
   
   @Override
   public CookieConfigDef name(String name)
   {
      cookieConfig().getOrCreate("name").text(name);
      return this;
   }

   @Override
   public CookieConfigDef domain(String domain)
   {
      cookieConfig().getOrCreate("domain").text(domain);
      return this;
   }

   @Override
   public CookieConfigDef path(String path)
   {
      cookieConfig().getOrCreate("path").text(path);
      return this;
   }

   @Override
   public CookieConfigDef comment(String comment)
   {
      cookieConfig().getOrCreate("comment").text(comment);
      return this;
   }

   @Override
   public CookieConfigDef httpOnly()
   {
      cookieConfig().getOrCreate("http-only").text(true);
      return this;
   }

   @Override
   public CookieConfigDef secure()
   {
      cookieConfig().getOrCreate("secure").text(true);
      return this;
   }

   @Override
   public CookieConfigDef maxAge(int maxAge)
   {
      cookieConfig().getOrCreate("max-age").text(maxAge);
      return this;
   }
}
