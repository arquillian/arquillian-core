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
package org.jboss.arquillian.protocol.servlet;

import java.net.URI;

import org.jboss.arquillian.spi.client.protocol.ProtocolConfiguration;

/**
 * ServletProtocolConfiguration
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletProtocolConfiguration implements ProtocolConfiguration
{
   private String host = "localhost";
   private Integer port = 8080;
   private String contextRoot = "test";
   
   /**
    * @return the host
    */
   public String getHost()
   {
      return host;
   }
   
   /**
    * @param host the host to set
    */
   public void setHost(String host)
   {
      this.host = host;
   }
   
   /**
    * @return the port
    */
   public Integer getPort()
   {
      return port;
   }
   
   /**
    * @param port the port to set
    */
   public void setPort(Integer port)
   {
      this.port = port;
   }
   
   /**
    * @return the context
    */
   public String getContextRoot()
   {
      return contextRoot;
   }

   /**
    * @param context the context to set
    */
   public void setContextRoot(String context)
   {
      this.contextRoot = context;
   }

   public URI getBaseURI()
   {
      return URI.create("http://" + host + ":" + port + "/" + contextRoot);
   }
}
