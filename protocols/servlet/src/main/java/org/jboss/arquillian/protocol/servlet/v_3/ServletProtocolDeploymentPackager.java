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
package org.jboss.arquillian.protocol.servlet.v_3;

import static org.jboss.arquillian.protocol.servlet.ServletUtil.APPLICATION_XML_PATH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.protocol.servlet.Processor;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.ApplicationDescriptor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;


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
   public Archive<?> generateDeployment(TestDeployment testDeployment, Collection<ProtocolArchiveProcessor> processors)
   {
      JavaArchive protocol = new ProtocolDeploymentAppender().createAuxiliaryArchive();
      
      Archive<?> applicationArchive = testDeployment.getApplicationArchive();
      Collection<Archive<?>> auxiliaryArchives = testDeployment.getAuxiliaryArchives();
      
      Processor processor = new Processor(testDeployment, processors);
      
      if(EnterpriseArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(EnterpriseArchive.class.cast(applicationArchive), auxiliaryArchives, protocol, processor);
      } 

      if(WebArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(WebArchive.class.cast(applicationArchive), auxiliaryArchives, protocol, processor);
      } 

      if(JavaArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(JavaArchive.class.cast(applicationArchive), auxiliaryArchives, protocol, processor);
      }

      throw new IllegalArgumentException(ServletProtocolDeploymentPackager.class.getName()  + 
            " can not handle archive of type " +  applicationArchive.getClass().getName());
   }

   private Archive<?> handleArchive(WebArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, JavaArchive protocol, Processor processor) 
   {
      applicationArchive
                  .addAsLibraries(
                        auxiliaryArchives.toArray(new Archive<?>[0]));
      
      // Can be null when reusing logic in EAR packaging
      if(protocol != null)
      {
         applicationArchive.addAsLibrary(protocol);
      }
      processor.process(applicationArchive);
      return applicationArchive;
   }

   private Archive<?> handleArchive(JavaArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, JavaArchive protocol, Processor processor) 
   {
      return handleArchive(
            ShrinkWrap.create(WebArchive.class, "test.war")
               .addAsLibrary(applicationArchive),
            auxiliaryArchives, 
            protocol,
            processor);
   }

   private Archive<?> handleArchive(EnterpriseArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, JavaArchive protocol, Processor processor) 
   {
      Map<ArchivePath, Node> applicationArchiveWars = applicationArchive.getContent(Filters.include(".*\\.war"));
      if(applicationArchiveWars.size() == 1)
      {
         ArchivePath warPath = applicationArchiveWars.keySet().iterator().next();
         try
         {
            handleArchive(
                  applicationArchive.getAsType(WebArchive.class, warPath), 
                  new ArrayList<Archive<?>>(), // reuse the War handling, but Auxiliary Archives should be added to the EAR, not the WAR 
                  protocol,
                  processor);
         }
         catch (IllegalArgumentException e) 
         {
            throw new IllegalArgumentException("Can not manipulate war's that are not of type " + WebArchive.class, e);
         }
      }
      else if(applicationArchiveWars.size() > 1)
      {
         // TODO: fetch the TestDeployment.getArchiveForEnrichment
         throw new UnsupportedOperationException("Multiple WebArchives found in " + applicationArchive.getName() + ". Can not determine which to enrich");
      }
      else
      {
         // reuse handle(JavaArchive, ..) logic
         Archive<?> wrappedWar = handleArchive(protocol, new ArrayList<Archive<?>>(), null, processor);
         applicationArchive
               .addAsModule(wrappedWar);
         
         if(applicationArchive.contains(APPLICATION_XML_PATH))
         {
            ApplicationDescriptor applicationXml = Descriptors.importAs(ApplicationDescriptor.class).from(
                  applicationArchive.get(APPLICATION_XML_PATH).getAsset().openStream());
            
            applicationXml.webModule(wrappedWar.getName(), wrappedWar.getName());
            
            // SHRINKWRAP-187, to eager on not allowing overrides, delete it first
            applicationArchive.delete(APPLICATION_XML_PATH);
            applicationArchive.setApplicationXML(
                  new StringAsset(applicationXml.exportAsString()));
         }
      }
      
      applicationArchive.addAsLibraries(
            auxiliaryArchives.toArray(new Archive<?>[0]));

      return applicationArchive;
   }
}
