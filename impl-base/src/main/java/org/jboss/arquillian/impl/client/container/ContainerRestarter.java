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
package org.jboss.arquillian.impl.client.container;

import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.container.AfterStart;
import org.jboss.arquillian.spi.event.container.AfterStop;
import org.jboss.arquillian.spi.event.container.BeforeStart;
import org.jboss.arquillian.spi.event.container.BeforeStop;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * A Handler for restarting the {@link DeployableContainer} for every X deployments.<br/>
 * <br/>
 *  <b>Fires:</b><br/>
 *   {@link BeforeStop}<br/>
 *   {@link AfterStop}<br/>
 *   {@link BeforeStart}<br/>
 *   {@link AfterStart}<br/>
 * <br/>
 *  <b>Imports:</b><br/>
 *   {@link DeployableContainer}<br/>
 *   
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 * @see DeployableContainer
 */
public class ContainerRestarter 
{
   private int deploymentCount = 0;
   
   @Inject
   private Instance<DeployableContainer<?>> deployableContainer;
   
   @Inject
   private Instance<Configuration> configuration;

   public void restart(@Observes BeforeClass event) throws Exception
   {
//      if(shouldRestart())
//      {
//         new ContainerStopper().callback(event);
//         new ContainerStarter().callback(event);
//      }
   }
   
//   private boolean shouldRestart()
//   {
//      Configuration configuration = context.get(Configuration.class); 
//      int maxDeployments = configuration == null ? -1:configuration.getMaxDeploymentsBeforeRestart();
//      if(maxDeployments > -1) 
//      {
//         if((maxDeployments -1 ) == deploymentCount)
//         {
//            deploymentCount = 0;
//            return true;
//         }
//      }
//      deploymentCount++;
//      return false;
//   }
}
