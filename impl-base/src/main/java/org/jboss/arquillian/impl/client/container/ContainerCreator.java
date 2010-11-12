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

import org.jboss.arquillian.impl.ThreadContext;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.container.AfterSetup;
import org.jboss.arquillian.spi.event.container.BeforeSetup;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;

/**
 * A Handler for creating and setting up a {@link DeployableContainer} for use. <br/>
 * <br/>
 *  <b>Fires:</b><br/>
 *   {@link BeforeSetup}<br/>
 *   {@link AfterSetup}<br/>
 * <br/>
 *  <b>Imports:</b><br/>
 *   {@link Configuration}<br/>
 *  <br/>
 *  <b>Exports:</b><br/>
 *   {@link DeployableContainer}<br/>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * 
 * @see Configuration
 * @see DeployableContainer
 */
public class ContainerCreator 
{
   @Inject 
   private Instance<ContainerContext> containerContext;

   @Inject 
   private Instance<ContainerRegistry> containerRegistry;

   @Inject
   private Event<BeforeSetup> before;
   
   @Inject
   private Event<AfterSetup> after;
   
   @Inject 
   private Instance<Injector> injector;
   
   public void create(@Observes BeforeSuite event) throws Exception 
   {
      for(Container container : containerRegistry.get().getContainers())
      {
         ThreadContext.set(container.getClassLoader());
         containerContext.get().activate(container.getName());
         try
         {
            DeployableContainer deployableContainer = container.getDeployableContainer();
            injector.get().inject(deployableContainer); // TODO: this should be moved to the ContianerCreator...
            
            before.fire(new BeforeSetup(deployableContainer));
            
            deployableContainer.setup(container.createDeployableConfiguration());
            
            after.fire(new AfterSetup(deployableContainer));
         } 
         finally 
         {
            containerContext.get().deactivate();
            ThreadContext.reset();
         }
      }         
   }
}
