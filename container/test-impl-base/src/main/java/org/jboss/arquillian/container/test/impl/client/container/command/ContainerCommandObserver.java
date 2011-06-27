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
package org.jboss.arquillian.container.test.impl.client.container.command;

import java.util.logging.Logger;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;

/**
 * ContainerCommandObserver
 *
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 */
public class ContainerCommandObserver
{
   private static final Logger log = Logger.getLogger(ContainerCommandObserver.class.getName());

   @Inject
   private Instance<ContainerController> controllerInst;
   
   @SuppressWarnings("rawtypes") // Generics not supported fully by core
   public void onException(@Observes EventContext<Command> event)
   {
      try
      {
         event.proceed();
      }
      catch (Exception e) 
      {
         event.getEvent().setThrowable(e);
      }
   }
   
   public void start(@Observes StartContainerCommand event)
   {
      try
      {
         if (event.getConfiguration() == null)
         {
            controllerInst.get().start(event.getContainerQualifier());
         }
         else
         {
            controllerInst.get().start(event.getContainerQualifier(), event.getConfiguration());
         }
         event.setResult("SUCCESS");
      }
      catch (Exception e) 
      {
         event.setResult("FAILED: " + e.getMessage());
      }
   }
   
   public void stop(@Observes StopContainerCommand event)
   {
      try
      {
         controllerInst.get().stop(event.getContainerQualifier());
         event.setResult("SUCCESS");
      }
      catch (Exception e) 
      {
         event.setResult("FAILED: " + e.getMessage());
      }
   }
   
   public void kill(@Observes KillContainerCommand event)
   {
      try
      {
         controllerInst.get().kill(event.getContainerQualifier());
         event.setResult("SUCCESS");
      }
      catch (Exception e) 
      {
         event.setResult("FAILED: " + e.getMessage());
      }
   }
   
}
