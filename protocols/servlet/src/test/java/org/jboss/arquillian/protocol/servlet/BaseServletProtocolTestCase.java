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

import junit.framework.Assert;

import org.jboss.arquillian.protocol.servlet.v_2_5.ServletProtocol;
import org.jboss.arquillian.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.junit.Test;


/**
 * ServletProtocolTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class BaseServletProtocolTestCase
{
   private BaseServletProtocol protocol = new BaseServletProtocol()
   {
      @Override
      protected String getProtcolName()
      {
         return "Test";
      }
      
      @Override
      public DeploymentPackager getPackager()
      {
         return null;
      }
   };

   @Test
   public void shouldFindTestServletInMetadata() throws Exception
   {
      ServletProtocolConfiguration config = new ServletProtocolConfiguration();
      
      ServletMethodExecutor executor = protocol.getExecutor(
            config, 
            new ProtocolMetaData()
               .addContext(
                     new HTTPContext("127.0.0.1", 8080)
                        .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "test"))));
      
      Assert.assertEquals("http://127.0.0.1:8080/test", executor.getBaseURI().toString());
   }
   
   @Test
   public void shouldOverrideMetadata() throws Exception
   {
      ServletProtocolConfiguration config = new ServletProtocolConfiguration();
      config.setHost("10.10.10.1");
      config.setPort(90);
      
      ServletProtocol protocol = new ServletProtocol();
      
      ServletMethodExecutor executor = protocol.getExecutor(
            config, 
            new ProtocolMetaData()
               .addContext(
                     new HTTPContext("127.0.0.1", 8080)
                        .add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "test"))));
      
      Assert.assertEquals("http://10.10.10.1:90/test", executor.getBaseURI().toString());
   }
}
