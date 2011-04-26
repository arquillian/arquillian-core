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
package org.jboss.arquillian.testenricher.cdi;

import java.util.Map;

import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.arquillian.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.ArchiveAsset;

/**
 * A {@link ProtocolArchiveProcessor} that will add beans.xml to the protocol unit if one is defined in the test deployment.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class BeansXMLProtocolProcessor implements ProtocolArchiveProcessor
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.deployment.ProtocolArchiveProcessor#process(org.jboss.arquillian.spi.TestDeployment, org.jboss.shrinkwrap.api.Archive)
    */
   @Override
   public void process(TestDeployment testDeployment, Archive<?> protocolArchive)
   {
      if(testDeployment.getApplicationArchive() == protocolArchive)
      {
         return; // if the protocol is merged in the user Archive, the user is in control.
      }
      
      if(containsBeansXML(testDeployment.getApplicationArchive()))
      {
         if(WebArchive.class.isInstance(protocolArchive))
         {
            WebArchive.class.cast(protocolArchive).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
         }
         else if(JavaArchive.class.isInstance(protocolArchive))
         {
            JavaArchive.class.cast(protocolArchive).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
         }
      }
   }
   
   private boolean containsBeansXML(Archive<?> archive)
   {
      Map<ArchivePath, Node> content = archive.getContent(Filters.include(".*/beans\\.xml"));
      if(!content.isEmpty())
      {
         return true;
      }
      Map<ArchivePath, Node> nested = archive.getContent(Filters.include("/.*\\.(jar|war)"));
      if(!nested.isEmpty())
      {
         for(Node node : nested.values())
         {
            if(node.getAsset() instanceof ArchiveAsset )
            {
               boolean containsBeansXML = containsBeansXML(
                     ((ArchiveAsset)node.getAsset()).getArchive());
               if(containsBeansXML)
               {
                  return true;
               }
            }
         }
      }
      return false;
   }
}
