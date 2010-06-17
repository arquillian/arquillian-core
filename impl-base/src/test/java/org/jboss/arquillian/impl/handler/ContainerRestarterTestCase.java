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

import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.container.AfterStart;
import org.jboss.arquillian.spi.event.container.AfterStop;
import org.jboss.arquillian.spi.event.container.BeforeStart;
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
 * ContainerRestarterTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerRestarterTestCase
{

   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock
   private DeployableContainer container;

   @Mock
   private EventHandler<ContainerEvent> eventHandler;

   @Test
   public void shouldRestartContainerForEveryX() throws Exception 
   {
      Configuration configuration = new Configuration();
      configuration.setMaxDeploymentsBeforeRestart(5);
      
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      context.add(Configuration.class, configuration);
      context.add(DeployableContainer.class, container);
      context.register(BeforeStart.class, eventHandler);
      context.register(AfterStart.class, eventHandler);
      context.register(BeforeStop.class, eventHandler);
      context.register(AfterStop.class, eventHandler);
      
      ContainerRestarter handler = new ContainerRestarter();
      
      for(int i = 0; i < 10; i++)
      {
         handler.callback(context, new SuiteEvent());
      }
      
      // verify that the container was restarted twice
      Mockito.verify(container, Mockito.times(2)).stop(context);
      Mockito.verify(container, Mockito.times(2)).start(context);
      
      // verify that all the events where fired (2 times restart * 4(2 start + 2 stop))
      Mockito.verify(eventHandler, Mockito.times(8)).callback(
            Mockito.any(SuiteContext.class), Mockito.any(ContainerEvent.class));
   }
   
   @Test
   public void shouldNotForceRestartIfMaxDeploymentsNotSet() throws Exception
   {
      Configuration configuration = new Configuration();
      configuration.setMaxDeploymentsBeforeRestart(-1);
      
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      context.add(Configuration.class, configuration);
      context.add(DeployableContainer.class, container);
      context.register(BeforeStart.class, eventHandler);
      context.register(AfterStart.class, eventHandler);
      context.register(BeforeStop.class, eventHandler);
      context.register(AfterStop.class, eventHandler);
      
      ContainerRestarter handler = new ContainerRestarter();
      
      for(int i = 0; i < 10; i++)
      {
         handler.callback(context, new SuiteEvent());
      }
      
      // verify that the container was restarted twice
      Mockito.verify(container, Mockito.times(0)).stop(context);
      Mockito.verify(container, Mockito.times(0)).start(context);
      
      // verify that all the events where fired (2 times restart * 4(2 start + 2 stop))
      Mockito.verify(eventHandler, Mockito.times(0)).callback(
            Mockito.any(SuiteContext.class), Mockito.any(ContainerEvent.class));
   }
}
