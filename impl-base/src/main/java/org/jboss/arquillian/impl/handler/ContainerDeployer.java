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

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.event.container.AfterDeploy;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.shrinkwrap.api.Archive;

/**
 * A Handler for creating and deploying the generated {@link Archive} to the container. <br/>
 * <br/>
 *  <b>Fires:</b><br/>
 *   {@link BeforeDeploy}<br/>
 *   {@link AfterDeploy}<br/>
 * <br/>
 *  <b>Imports:</b><br/>
 *   {@link DeployableContainer}<br/>
 *   {@link Archive}<br/>
 *  <br/>
 *  <b>Exports:</b><br/>
 *   {@link ContainerMethodExecutor}<br/>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * 
 * @see DeployableContainer
 * @see ContainerMethodExecutor
 * @see Archive 
 */
public class ContainerDeployer implements EventHandler<ClassEvent>
{
   
   public void callback(Context context, ClassEvent event) throws Exception
   {
      DeployableContainer container = context.get(DeployableContainer.class);
      Validate.stateNotNull(container, "No " + DeployableContainer.class.getName() + " found in context");
      
      Archive<?> deployment = context.get(Archive.class);
      Validate.stateNotNull(deployment, "No " + Archive.class.getName() + " found in context");
      
      context.fire(new BeforeDeploy());
      ContainerMethodExecutor executor = container.deploy(context, deployment);
      context.add(ContainerMethodExecutor.class, executor);
      context.fire(new AfterDeploy());
   }
}
