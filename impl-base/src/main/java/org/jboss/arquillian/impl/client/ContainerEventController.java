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
package org.jboss.arquillian.impl.client;

import org.jboss.arquillian.impl.client.container.event.ContainerMultiControlEvent;
import org.jboss.arquillian.impl.client.container.event.DeployManagedDeployments;
import org.jboss.arquillian.impl.client.container.event.SetupContainers;
import org.jboss.arquillian.impl.client.container.event.StartManagedContainers;
import org.jboss.arquillian.impl.client.container.event.StopManagedContainers;
import org.jboss.arquillian.impl.client.container.event.UnDeployManagedDeployments;
import org.jboss.arquillian.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;

/**
 * Event dispatcher between Test lifecyle events and Container control events.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerEventController
{
   @Inject
   private Event<ContainerMultiControlEvent> container;
   
   @Inject
   private Event<GenerateDeployment> deployment;

   /*
    * Suite Level
    */
   public void execute(@Observes BeforeSuite event)
   {
      container.fire(new SetupContainers());
      container.fire(new StartManagedContainers());
   }

   public void execute(@Observes AfterSuite event)
   {
      container.fire(new StopManagedContainers());
   }

   /*
    * Class Level
    */
   public void execute(@Observes BeforeClass event)
   {
      deployment.fire(new GenerateDeployment(event.getTestClass()));
      container.fire(new DeployManagedDeployments());
   }

   public void execute(@Observes AfterClass event)
   {
      container.fire(new UnDeployManagedDeployments());
   }
}
