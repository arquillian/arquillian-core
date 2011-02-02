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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jboss.arquillian.protocol.servlet.Processor;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.arquillian.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.spi.client.deployment.ProtocolArchiveProcessor;
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
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
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
   public Archive<?> generateDeployment(TestDeployment testDeployment, Collection<ProtocolArchiveProcessor> processors)
   {
      WebArchive protocol = new ProtocolDeploymentAppender().createAuxiliaryArchive();
      
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

   private Archive<?> handleArchive(WebArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, WebArchive protocol, Processor processor) 
   {
      ArchivePath webXmlPath = ArchivePaths.create("WEB-INF/web.xml");
      if(applicationArchive.contains(webXmlPath))
      {
         WebAppDescriptor applicationWebXml = Descriptors.importAs(WebAppDescriptor.class).from(
               applicationArchive.get(webXmlPath).getAsset().openStream());
         
         applicationArchive.setWebXML(
               new StringAsset(
                     mergeWithDescriptor(applicationWebXml).exportAsString()));
         applicationArchive.merge(protocol, Filters.exclude(".*web\\.xml.*"));
      }
      else 
      {
         applicationArchive.merge(protocol);
      }
      applicationArchive.addLibraries(auxiliaryArchives.toArray(new Archive<?>[0]));
      
      processor.process(applicationArchive);
      return applicationArchive;
   }
   
   /*
    * Wrap the applicationArchive as a EnterpriseArchive and pass it to handleArchive(EnterpriseArchive, ...)
    */
   private Archive<?> handleArchive(JavaArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, WebArchive protocol, Processor processor) 
   {
      return handleArchive(
            ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
               .addModule(applicationArchive), 
            auxiliaryArchives, 
            protocol,
            processor);
   }

   private Archive<?> handleArchive(EnterpriseArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, WebArchive protocol, Processor processor) 
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
                  protocol,
                  processor);
         }
      }
      else if(applicationArchiveWars.size() > 1)
      {
         // TODO: fetch the TestDeployment.getArchiveForEnrichment
         throw new UnsupportedOperationException("Multiple WebArchives found in " + applicationArchive.getName() + ". Can not determine which to enrich");
      }
      else
      {
         applicationArchive
               .addModule(
                     protocol.setWebXML(
                           new StringAsset(createNewDescriptor().exportAsString())));
         
         ArchivePath applicationXmlPath = ArchivePaths.create("META-INF/application.xml");
         if(applicationArchive.contains(applicationXmlPath))
         {
            ApplicationDescriptor applicationXml = Descriptors.importAs(ApplicationDescriptor.class).from(
                  applicationArchive.get(applicationXmlPath).getAsset().openStream());
            
            applicationXml.webModule(protocol.getName(), protocol.getName());
            applicationArchive.setApplicationXML(
                  new StringAsset(applicationXml.exportAsString()));
         }
         
         processor.process(protocol);
      }
      applicationArchive.addLibraries(
            auxiliaryArchives.toArray(new Archive<?>[0]));
      return applicationArchive;
   }
   
   private WebAppDescriptor createNewDescriptor()
   {
      return mergeWithDescriptor(getDefaultDescriptor());
   }
   
   private WebAppDescriptor getDefaultDescriptor() 
   {
      return Descriptors.create(WebAppDescriptor.class)
                  .version("2.5")
                  .displayName("Arquillian Servlet 2.5 Protocol");
   }
   
   private WebAppDescriptor mergeWithDescriptor(WebAppDescriptor descriptor) 
   {
      // use String v. of desc.servlet(..) so we don't force Servlet API on classpath
      descriptor.servlet(
            ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME,
            "org.jboss.arquillian.protocol.servlet.runner.ServletTestRunner",  
            new String[]{ServletMethodExecutor.ARQUILLIAN_SERVLET_MAPPING});
      return descriptor;
   }   
}
