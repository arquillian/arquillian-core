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
package org.jboss.arquillian.spi.client.protocol.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * WebContext
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class HTTPContext
{
   private String host;
   private int port;
   
   private List<Servlet> servlets;
   
   public HTTPContext(String host, int port)
   {
      this.host = host;
      this.port = port;
      this.servlets = new ArrayList<Servlet>();
   }

   /**
    * @return the ip
    */
   public String getHost()
   {
      return host;
   }

   /**
    * @return the port
    */
   public int getPort()
   {
      return port;
   }

   public HTTPContext add(Servlet servlet)
   {
      servlet.setParent(this);
      this.servlets.add(servlet);
      return this;
   }
   
   /**
    * @return the servlets
    */
   public List<Servlet> getServlets()
   {
      return servlets;
   }
   
   public Servlet getServletByName(String name)
   {
      for(Servlet servlet : getServlets())
      {
         if(servlet.getName().equals(name))
         {
            return servlet;
         }
      }
      return null;
   }
   
}
