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
package org.jboss.arquillian.impl.handler;

import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.impl.ApplicationArchiveDeploymentGenerator;
import org.jboss.arquillian.impl.ClientDeploymentGenerator;
import org.jboss.arquillian.impl.DeploymentGenerator;
import org.jboss.arquillian.spi.ApplicationArchiveGenerator;
import org.jboss.arquillian.spi.ApplicationArchiveProcessor;
import org.jboss.arquillian.spi.AuxiliaryArchiveAppender;
import org.jboss.arquillian.spi.AuxiliaryArchiveProcessor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * Handler that will setup the context as defined by the {@link RunModeType}.<br/>
 * <b>{@link RunModeType#AS_CLIENT}</b>: Binds the {@link ApplicationArchiveDeploymentGenerator}, a {@link DeploymentGenerator} that only 
 * use the {@link ApplicationArchiveGenerator} SPI.<br/>
 * <b>{@link RunModeType#IN_CONTAINER}</b>: Binds the {@link ClientDeploymentGenerator}, a {@link DeploymentGenerator} that use the 
 * full Packager SPI. {@link DeploymentPackager}, {@link ApplicationArchiveGenerator}, {@link ApplicationArchiveProcessor}, {@link AuxiliaryArchiveAppender} and
 * {@link AuxiliaryArchiveProcessor}<br/>
 * <br/>  
 * 
 *  <b>Exports:</b><br/>
 *   {@link DeploymentGenerator}<br/>
 *   
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ActivateRunModeTypeDeployment extends AbstractRunModeHandler<BeforeClass>
{

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.handler.AbstractRunModeHandler#hasLocalRunMode(org.jboss.arquillian.spi.Context)
    */
   @Override
   protected void hasClientRunMode(Context context)
   {
      context.add(DeploymentGenerator.class, new ApplicationArchiveDeploymentGenerator(context.getServiceLoader()));
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.handler.AbstractRunModeHandler#hasRemoteRunMode(org.jboss.arquillian.spi.Context)
    */
   @Override
   protected void hasContainerRunMode(Context context)
   {
      context.add(DeploymentGenerator.class, new ClientDeploymentGenerator(context.getServiceLoader()));      
   }
}
