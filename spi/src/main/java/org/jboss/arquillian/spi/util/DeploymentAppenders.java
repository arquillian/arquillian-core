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
package org.jboss.arquillian.spi.util;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.spi.DeploymentAppender;
import org.jboss.shrinkwrap.api.Archive;

/**
 * DeploymentAppenders
 * 
 * Helper class for getting all Archives that should be added to the deployment. 
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class DeploymentAppenders
{
   private DeploymentAppenders() { }
   
   /**
    * Load/Create all Archives provided by the different modules. 
    * 
    * @return A List of all archives
    */
   public static List<Archive<?>> getArchives() 
   {
      List<Archive<?>> archives = new ArrayList<Archive<?>>();
      DefaultServiceLoader<DeploymentAppender> serviceLoader = DefaultServiceLoader.load(
            DeploymentAppender.class);
   
      for(DeploymentAppender deploymentAppender : serviceLoader)
      {
         archives.add(deploymentAppender.createArchive());
      }
      return archives;
   }
   
}
