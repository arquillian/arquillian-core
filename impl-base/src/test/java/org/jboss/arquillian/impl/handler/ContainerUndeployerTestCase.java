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
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.spi.event.container.ContainerEvent;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * UnDeploymentHandlerTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerUndeployerTestCase
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
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      
      ContainerUndeployer handler = new ContainerUndeployer();
      handler.callback(context, new ClassEvent(getClass()));
   }

   @Test(expected = IllegalStateException.class)
   public void shouldThrowIllegalStateOnMissingArchive() throws Exception
   {
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      context.add(DeployableContainer.class, container);
      
      ContainerUndeployer handler = new ContainerUndeployer();
      handler.callback(context, new ClassEvent(getClass()));
   }
   
   @Test
   public void shouldUndeployArchive() throws Exception
   {
      Archive<?> deployment = ShrinkWrap.create("test.jar", JavaArchive.class);
      
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      context.add(DeployableContainer.class, container);
      context.add(Archive.class, deployment);
      context.register(BeforeUnDeploy.class, eventHandler);
      context.register(AfterUnDeploy.class, eventHandler);

      ContainerUndeployer handler = new ContainerUndeployer();
      handler.callback(context, new ClassEvent(getClass()));
      
      // verify that the deployment was undeployed from the container
      Mockito.verify(container).undeploy(context, deployment);

      // verify that all the events where fired
      Mockito.verify(eventHandler, Mockito.times(2)).callback(
            Mockito.any(SuiteContext.class), Mockito.any(ContainerEvent.class));
   }
}
