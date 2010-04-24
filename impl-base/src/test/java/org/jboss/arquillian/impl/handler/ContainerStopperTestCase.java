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

import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.container.AfterStop;
import org.jboss.arquillian.spi.event.container.BeforeStop;
import org.jboss.arquillian.spi.event.container.ContainerEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * StopContainerTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerStopperTestCase
{
   @Mock
   private ServiceLoader serviceLoader;

   @Mock
   private DeployableContainer container;

   @Mock
   private EventHandler<ContainerEvent> eventHandler;

   @Test(expected = IllegalStateException.class)
   public void shouldThrowIllegalStateOnMissingDeployableContainer() throws Exception
   {
      SuiteContext context = new SuiteContext(serviceLoader);
      
      ContainerStopper handler = new ContainerStopper();
      handler.callback(context, new SuiteEvent());
   }
   
   @Test
   public void shouldStartTheDeployableContainer() throws Exception
   {
      SuiteContext context = new SuiteContext(serviceLoader);
      context.add(DeployableContainer.class, container);
      context.register(BeforeStop.class, eventHandler);
      context.register(AfterStop.class, eventHandler);

      ContainerStopper handler = new ContainerStopper();
      handler.callback(context, new SuiteEvent());
      
      // verify that the container was stopped
      Mockito.verify(container).stop(context);

      // verify that all the events where fired
      Mockito.verify(eventHandler, Mockito.times(2)).callback(
            Mockito.any(SuiteContext.class), Mockito.any(ContainerEvent.class));
   }
}
