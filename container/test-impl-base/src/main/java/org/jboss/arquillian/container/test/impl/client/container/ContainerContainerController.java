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
package org.jboss.arquillian.container.test.impl.client.container;

import java.util.Map;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.impl.client.container.command.KillContainerCommand;
import org.jboss.arquillian.container.test.impl.client.container.command.StartContainerCommand;
import org.jboss.arquillian.container.test.impl.client.container.command.StopContainerCommand;
import org.jboss.arquillian.container.test.spi.command.CommandService;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * ClientContainerController
 *
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 */
public class ContainerContainerController implements ContainerController
{
   @Inject
   private Instance<ServiceLoader> serviceLoader;
   
   @Override
   public void start(String containerQualifier) 
   {
      getCommandService().execute(new StartContainerCommand(containerQualifier));
   }
   
   @Override
   public void start(String containerQualifier, Map<String, String> config)
   {
      getCommandService().execute(new StartContainerCommand(containerQualifier, config));
   }

   @Override
   public void stop(String containerQualifier) 
   {
      getCommandService().execute(new StopContainerCommand(containerQualifier));
   }
   
   @Override
   public void kill(String containerQualifier)
   {
      getCommandService().execute(new KillContainerCommand(containerQualifier));
   }
   
   private CommandService getCommandService()
   {
      ServiceLoader loader = serviceLoader.get();
      if(loader == null)
      {
         throw new IllegalStateException("No " + ServiceLoader.class.getName() + " found in context");
      }
      CommandService service = loader.onlyOne(CommandService.class);
      if(service == null)
      {
         throw new IllegalStateException("No " + CommandService.class.getName() + " found in context");
      }
      return service;
   }
}
