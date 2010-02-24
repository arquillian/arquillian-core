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
package org.jboss.arquillian.testenricher.jboss;

import org.jboss.arquillian.spi.AuxiliaryArchiveAppender;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * EmbeddedDeploymentAppender
 * 
 * Package the required dependencies needed by the Jboss Embedded Container plugin 
 * to run in container. 
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JbossDeploymentAppender implements AuxiliaryArchiveAppender
{

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeploymentAppender#createArchive()
    */
   @Override
   public Archive<?> createAuxiliaryArchive()
   {
      JavaArchive archive = Archives.create("jboss-testenrichers.jar", JavaArchive.class)
                        .addPackages(
                              true, 
                              Package.getPackage("org.jboss.arquillian.testenricher.jboss"))
                        .addServiceProvider(
                              TestEnricher.class, 
                              EJBInjectionEnricher.class,
                              ResourceInjectionEnricher.class,
                              CDIInjectionEnricher.class);
      return archive;
   }

}
