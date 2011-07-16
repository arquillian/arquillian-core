/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.protocol.servlet.v_2_5;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.WebAppDescriptor;
import org.jboss.arquillian.protocol.servlet.runner.SecurityActions;
import org.jboss.arquillian.protocol.servlet.runner.ServletRemoteExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * ProtocolDeploymentAppender
 * 
 * DeploymentAppender to add required resources for the protocol servlet to run  
 * in container.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ProtocolDeploymentAppender implements AuxiliaryArchiveAppender
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.AuxiliaryArchiveAppender#createAuxiliaryArchive()
    */
   @Override
   public WebArchive createAuxiliaryArchive()
   {
      // Load based on package to avoid ClassNotFoundException on HttpServlet when loading ServletTestRunner
      return ShrinkWrap.create(WebArchive.class, "arquillian-protocol.war")
                     .addPackage(SecurityActions.class.getPackage())
                     .setWebXML(new StringAsset(
                           Descriptors.create(WebAppDescriptor.class)
                              .version("2.5")
                              .displayName("Arquillian")
                              .servlet(
                                    "org.jboss.arquillian.protocol.servlet.runner.ServletTestRunner", 
                                    ServletMethodExecutor.ARQUILLIAN_SERVLET_MAPPING)
                              .exportAsString()
                     ))
                     .addAsServiceProvider(RemoteLoadableExtension.class, ServletRemoteExtension.class);
   }
}
