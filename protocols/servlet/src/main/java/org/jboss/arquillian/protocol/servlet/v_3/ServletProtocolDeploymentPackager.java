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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentPackager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor;
import org.jboss.shrinkwrap.impl.base.asset.ArchiveAsset;

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
      JavaArchive protocol = new ProtocolDeploymentAppender().createAuxiliaryArchive();
      
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

   private Archive<?> handleArchive(WebArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, JavaArchive protocol) 
   {
      applicationArchive
                  .addLibraries(
                        auxiliaryArchives.toArray(new Archive<?>[0]));
      
      // Can be null when reusing logic in EAR packaging
      if(protocol != null)
      {
         applicationArchive.addLibrary(protocol);
      }
      return applicationArchive;
   }

   private Archive<?> handleArchive(JavaArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, JavaArchive protocol) 
   {
      return handleArchive(
            ShrinkWrap.create(WebArchive.class, "test.war")
               .addLibrary(applicationArchive),
            auxiliaryArchives, 
            protocol);
   }

   private Archive<?> handleArchive(EnterpriseArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, JavaArchive protocol) 
   {
      Map<ArchivePath, Node> applicationArchiveWars = applicationArchive.getContent(Filters.include(".*\\.war"));
      if(applicationArchiveWars.size() == 1)
      {
         // TODO: fix, relies on internal SW details, find web archive and attach our self to it, SHRINKWRAP-192         
         Asset warAsset = applicationArchiveWars.values().iterator().next().getAsset();
         if (warAsset instanceof ArchiveAsset)
         {
            ArchiveAsset warArchiveAsset = (ArchiveAsset) warAsset;
            handleArchive(
                  warArchiveAsset.getArchive().as(WebArchive.class), 
                  new ArrayList<Archive<?>>(), // reuse the War handling, but Auxiliary Archives should be added to the EAR, not the WAR 
                  protocol);
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
         Archive<?> wrappedWar = handleArchive(protocol, new ArrayList<Archive<?>>(), null);
         applicationArchive
               .addModule(wrappedWar);
         
         ArchivePath applicationXmlPath = ArchivePaths.create("META-INF/application.xml");
         if(applicationArchive.contains(applicationXmlPath))
         {
            ApplicationDescriptor applicationXml = Descriptors.importAs(ApplicationDescriptor.class).from(
                  applicationArchive.get(applicationXmlPath).getAsset().openStream());
            
            applicationXml.webModule(wrappedWar.getName(), wrappedWar.getName());
            applicationArchive.setApplicationXML(
                  new StringAsset(applicationXml.exportAsString()));
         }
      }
      
      applicationArchive.addLibraries(
            auxiliaryArchives.toArray(new Archive<?>[0]));

      return applicationArchive;
   }
}
