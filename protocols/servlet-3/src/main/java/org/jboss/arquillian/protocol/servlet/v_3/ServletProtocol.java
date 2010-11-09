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
package org.jboss.arquillian.protocol.servlet.v_3;

import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.protocol.servlet.ServletProtocolConfiguration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.spi.client.protocol.Protocol;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;

/**
 * ServletProtocol
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletProtocol implements Protocol<ServletProtocolConfiguration>
{
   private static final String PROTOCOL_NAME = "Servlet 3.0";
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.protocol.Protocol#getProtocolConfigurationClass()
    */
   public Class<ServletProtocolConfiguration> getProtocolConfigurationClass()
   {
      return ServletProtocolConfiguration.class;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.protocol.Protocol#getDescription()
    */
   public ProtocolDescription getDescription()
   {
      return new ProtocolDescription(PROTOCOL_NAME);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.protocol.Protocol#getPackager()
    */
   public DeploymentPackager getPackager()
   {
      return new ServletProtocolDeploymentPackager();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.protocol.Protocol#getExecutor(org.jboss.arquillian.spi.client.protocol.ProtocolConfiguration, org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData)
    */
   public ContainerMethodExecutor getExecutor(ServletProtocolConfiguration protocolConfiguration, ProtocolMetaData metaData)
   {
      if( metaData.hasContext(HTTPContext.class))
      {
         HTTPContext context = metaData.getContext(HTTPContext.class);
         return new ServletMethodExecutor(context.getBaseURI());
      }
      
      return new ServletMethodExecutor(protocolConfiguration.getBaseURI());
   }
}