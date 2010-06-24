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
package org.jboss.arquillian.packager.osgi;

import java.util.Collection;

import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Packager for running Arquillian against OSGi containers.
 *
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiDeploymentPackager implements DeploymentPackager
{
   public Archive<?> generateDeployment(Archive<?> applicationArchive, Collection<Archive<?>> auxiliaryArchives)
   {
      if(JavaArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(JavaArchive.class.cast(applicationArchive), auxiliaryArchives);
      }
      
      throw new IllegalArgumentException(OSGiDeploymentPackager.class.getName()  + 
            " can not handle archive of type " +  applicationArchive.getClass().getName());
   }

   private Archive<?> handleArchive(JavaArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives) 
   {
      if(containsArchiveOfType(WebArchive.class, auxiliaryArchives))
      {
         EnterpriseArchive deployment = ShrinkWrap.create("test.ear", EnterpriseArchive.class)
                                             .addModule(applicationArchive);
         for (Archive<?> moduleArchive : auxiliaryArchives)
         {
            if (WebArchive.class.isInstance(moduleArchive))
            {
               deployment.addModule(moduleArchive);
            } 
            else
            {
               deployment.addLibrary(moduleArchive);
            }
         }
         return deployment;
      }
      else 
      {
         WebArchive deployment = ShrinkWrap.create("test.war", WebArchive.class);
         deployment.addLibraries(auxiliaryArchives.toArray(new Archive[0]));
         deployment.addLibraries(applicationArchive);
         return deployment;
      }
   }

   private Archive<?> handleArchive(EnterpriseArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives) 
   {
      if(!containsArchiveOfType(WebArchive.class, auxiliaryArchives))
      {
         for (Archive<?> moduleArchive : auxiliaryArchives)
         {
            if ("arquillian-protocol.jar".equals(moduleArchive.getName()) && 
                  JavaArchive.class.isInstance(moduleArchive))
            {
               applicationArchive.addModule(
                     ShrinkWrap.create("test.war", WebArchive.class)
                              .addLibraries(moduleArchive));
            } 
            else
            {
               applicationArchive.addLibrary(moduleArchive);
            }
         }
      }
      else
      {
         for (Archive<?> moduleArchive : auxiliaryArchives)
         {
            if (WebArchive.class.isInstance(moduleArchive))
            {
               applicationArchive.addModule(moduleArchive);
            } 
            else
            {
               applicationArchive.addLibrary(moduleArchive);
            }
         }
      }
      return applicationArchive;
   }
   
   private boolean containsArchiveOfType(Class<? extends Archive<?>> clazz, Collection<Archive<?>> archives) 
   {
      for(Archive<?> archive : archives)
      {
         if(clazz.isInstance(archive))
         {
            return true;
         }
      }
      return false;
   }
}