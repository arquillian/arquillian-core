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
package org.jboss.arquillian.container.test.impl.client.deployment.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;

/**
 * DeployDeploymentObserver
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentCommandObserver
{
   private static final Logger log = Logger.getLogger(DeploymentCommandObserver.class.getName());

   @Inject
   private Instance<Deployer> deployerInst;
   
   @SuppressWarnings({ "rawtypes", "unchecked" }) // Generics not supported fully by core
   public void onException(@Observes EventContext<Command> event)
   {
      try
      {
         event.proceed();
      }
      catch (Exception e) 
      {
         event.getEvent().setResult("FAILED: " + e.getMessage());
         event.getEvent().setThrowable(e);
      }
   }
   
   public void deploy(@Observes DeployDeploymentCommand event)
   {
      deployerInst.get().deploy(event.getDeploymentName());
      event.setResult("SUCCESS");
   }

   public void undeploy(@Observes UnDeployDeploymentCommand event)
   {
      deployerInst.get().undeploy(event.getDeploymentName());
      event.setResult("SUCCESS");
   }
   
   public void getDeployment(@Observes GetDeploymentCommand event)
   {
      byte[] deploymentContent = asByteArray(
            deployerInst.get().getDeployment(
                  event.getDeploymentName()));
      event.setResult(deploymentContent);
   }
   

   /**
    * Obtains the contents of the specified stream
    * as a byte array
    *
    * @param in InputStream
    * @throws IllegalArgumentException If the stream was not specified
    * @return the byte[] for the given InputStream
    */
   static byte[] asByteArray(final InputStream in) throws IllegalArgumentException
   {
      // Precondition check
      if (in == null) {
         throw new IllegalArgumentException("stream must be specified");
      }

      // Get content as an array of bytes
      final ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
      final int len = 4096;
      final byte[] buffer = new byte[len];
      int read = 0;
      try {
         while (((read = in.read(buffer)) != -1)) {
            out.write(buffer, 0, read);
         }
      }
      catch (final IOException ioe) {
         throw new RuntimeException("Error in obtainting bytes from " + in, ioe);
      }
      finally {
         try {
            in.close();
         }
         catch (final IOException ignore) {
            if (log.isLoggable(Level.FINER)) {
               log.finer("Could not close stream due to: " + ignore.getMessage() + "; ignoring");
            }
         }
         // We don't need to close the outstream, it's a byte array out
      }

      // Represent as byte array
      final byte[] content = out.toByteArray();

      // Return
      return content;
   }
}
