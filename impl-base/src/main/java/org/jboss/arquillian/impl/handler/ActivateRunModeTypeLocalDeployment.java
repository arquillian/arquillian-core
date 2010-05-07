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
import org.jboss.arquillian.impl.DeploymentGenerator;
import org.jboss.arquillian.spi.ApplicationArchiveGenerator;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Handler that will setup the context as defined by the {@link RunModeType#LOCAL}. <br/>
 * Will override the normal DeploymentPackager with a version that ignores appenders/processors and packagers.<br/> 
 * <br/>  
 * 
 *  <b>Exports:</b><br/>
 *   {@link DeploymentGenerator}<br/>
 *   
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ActivateRunModeTypeLocalDeployment extends AbstractRunModeHandler<BeforeClass>
{

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.handler.AbstractRunModeHandler#hasLocalRunMode(org.jboss.arquillian.spi.Context)
    */
   @Override
   protected void hasLocalRunMode(Context context)
   {
      context.add(DeploymentGenerator.class, new ApplicationArchiveDeployment(context.getServiceLoader()));
   }

   /**
    *
    * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
    * @version $Revision: $
    */
   private class ApplicationArchiveDeployment implements DeploymentGenerator
   {
      private ServiceLoader serviceLoader;
      
      public ApplicationArchiveDeployment(ServiceLoader serviceLoader)
      {
         this.serviceLoader = serviceLoader;
      }
      
      public Archive<?> generate(Class<?> testCase)
      {
         return serviceLoader.onlyOne(ApplicationArchiveGenerator.class).generateApplicationArchive(testCase);
      }
   }
}
