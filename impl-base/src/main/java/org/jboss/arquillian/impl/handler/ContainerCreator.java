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
import org.jboss.arquillian.impl.event.EventHandler;
import org.jboss.arquillian.impl.event.type.SuiteEvent;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.DeployableContainer;

/**
 * A Handler for creating and setting up a {@link DeployableContainer} for use. <br/>
 * <br/>
 * 
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
public class ContainerCreator implements EventHandler<SuiteContext, SuiteEvent>
{
   
   public void callback(SuiteContext context, SuiteEvent event) throws Exception 
   {
      DeployableContainer container = context.getServiceLoader().onlyOne(DeployableContainer.class);
      container.setup(context.get(Configuration.class));
      
      context.add(DeployableContainer.class, container);
   }
}
