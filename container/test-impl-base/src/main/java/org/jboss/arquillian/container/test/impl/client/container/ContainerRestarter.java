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
package org.jboss.arquillian.container.test.impl.client.container;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.event.ContainerMultiControlEvent;
import org.jboss.arquillian.container.spi.event.StartSuiteContainers;
import org.jboss.arquillian.container.spi.event.StopSuiteContainers;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * A Handler for restarting the containers for every X test class.<br/>
 * <br/>
 *  <b>Fires:</b><br/>
 *   {@link StopSuiteContainers}<br/>
 *   {@link StartSuiteContainers}<br/>
 * <br/>
 *  <b>Imports:</b><br/>
 *   {@link ArquillianDescriptor}<br/>
 *   
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerRestarter 
{
   private int testClassesCount = 0;
   
   @Inject
   private Event<ContainerMultiControlEvent> controlEvent;
   
   @Inject
   private Instance<ArquillianDescriptor> configuration;

   public void restart(@Observes BeforeClass event) throws Exception
   {
      if(shouldRestart())
      {
         controlEvent.fire(new StopSuiteContainers());
         controlEvent.fire(new StartSuiteContainers());
      }
   }
   
   private boolean shouldRestart()
   {
      ArquillianDescriptor descriptor = configuration.get(); 
      Integer maxTestClasses = descriptor.engine().getMaxTestClassesBeforeRestart();
      if(maxTestClasses == null)
      {
         return false;
      }
      if(maxTestClasses > -1) 
      {
         if((maxTestClasses -1 ) == testClassesCount)
         {
            testClassesCount = 0;
            return true;
         }
      }
      testClassesCount++;
      return false;
   }
}
