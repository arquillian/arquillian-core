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
package org.jboss.arquillian.protocol.servlet.v_2_5;

import java.util.Collection;

import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentPackager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * ServletProtocolDeploymentPackager
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletProtocolDeploymentPackager implements DeploymentPackager
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeploymentPackager#generateDeployment(org.jboss.arquillian.spi.TestDeployment)
    */
   public Archive<?> generateDeployment(TestDeployment testDeployment)
   {
      WebArchive protocol = WebArchive.class.cast(
            new ProtocolDeploymentAppender().createAuxiliaryArchive());
      
      Archive<?> applicationArchive = testDeployment.getApplicationArchive();
      Collection<Archive<?>> auxiliaryArchives = testDeployment.getAuxiliaryArchives();
      
      if(EnterpriseArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(EnterpriseArchive.class.cast(applicationArchive), auxiliaryArchives, protocol);
      } 

      if(WebArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(WebArchive.class.cast(applicationArchive), auxiliaryArchives, protocol);
      } 

      if(JavaArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(JavaArchive.class.cast(applicationArchive), auxiliaryArchives, protocol);
      }

      throw new IllegalArgumentException(ServletProtocolDeploymentPackager.class.getName()  + 
            " can not handle archive of type " +  applicationArchive.getClass().getName());
   }

   private Archive<?> handleArchive(WebArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, WebArchive protocol) 
   {
      throw new IllegalArgumentException("The " + ServletProtocolDeploymentPackager.class.getSimpleName() + " can't merge web.xml files.");
   }

   private Archive<?> handleArchive(JavaArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, WebArchive protocol) 
   {
      return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                        .addModule(applicationArchive)
                        .addModule(protocol)
                        .addLibraries(auxiliaryArchives.toArray(new Archive[0]));
   }

   private Archive<?> handleArchive(EnterpriseArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, WebArchive protocol) 
   {
      if(false) // contains web archive
      {
         // find web archive and attach our self to it
      }
      else
      {
         applicationArchive
               .addModule(protocol)
               .addLibraries(
                     auxiliaryArchives.toArray(new Archive<?>[0]));
      }
      return applicationArchive;
   }
}
