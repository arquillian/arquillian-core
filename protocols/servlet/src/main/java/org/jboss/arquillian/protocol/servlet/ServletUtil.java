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
package org.jboss.arquillian.protocol.servlet;

import java.net.URI;

import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;

/**
 * ServletUtil
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class ServletUtil
{
   private ServletUtil() {}
   
   public static URI determineBaseURI(ServletProtocolConfiguration config, ProtocolMetaData metaData, String servletName)
   {
      String address = config.getHost();
      Integer port = config.getPort();

      // TODO: can not set contextRoot in config, change to prefixContextRoot
      String contextRoot = null; //protocolConfiguration.getContextRoot(); 
      
      if( metaData.hasContext(HTTPContext.class))
      {
         HTTPContext context = metaData.getContext(HTTPContext.class);
         Servlet servlet = context.getServletByName(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME);
         if(servlet != null)
         {
            // use the context where the Arquillian servlet is found
            if(address == null)
            {
               address = context.getHost();
            }
            if(port == null)
            {
               port = context.getPort();
            }
            contextRoot = servlet.getContextRoot();
         }
         else
         {
            throw new IllegalArgumentException(
                  ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME + " not found. " +
                  "Could not determine ContextRoot from ProtocolMetadata, please contact DeployableContainer developer.");
         }
      }
      else
      {
         throw new IllegalArgumentException(
               "No " + HTTPContext.class.getName() + " found in " + ProtocolMetaData.class.getName() + ". " +
               "Servlet protocol can not be used");
      }
      return URI.create("http://" + address + ":" + port + contextRoot);
   }
}
