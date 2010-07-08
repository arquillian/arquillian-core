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
package org.jboss.arquillian.impl;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.spi.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * ArquillianDeploymentAppender
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianDeploymentAppender implements AuxiliaryArchiveAppender
{
   
   public Archive<?> createAuxiliaryArchive()
   {
      
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class ,"arquillian-core.jar")
                        .addPackages(
                              true,
                              //Package.getPackage("org.jboss.arquillian.api"), // TODO: figure out why this does not work.. 
                              Deployment.class.getPackage(),
                              Package.getPackage("org.jboss.arquillian.impl"),
                              Package.getPackage("org.jboss.arquillian.spi"),
                              Package.getPackage("org.jboss.shrinkwrap.api"),
                              Package.getPackage("org.jboss.shrinkwrap.impl.base"),
                              Package.getPackage("org.jboss.shrinkwrap.spi"));
      return archive;
   }

}
