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

import java.util.List;

import org.jboss.arquillian.spi.util.DeploymentAppenders;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * DeploymentAppenderArchiveGenerator
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentAppenderArchiveGenerator implements ArchiveGenerator
{
   private ArchiveGenerator generator;
   
   public DeploymentAppenderArchiveGenerator(ArchiveGenerator generator)
   {
      Validate.notNull(generator, "Generator must be specified");
      this.generator = generator;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.ArchiveGenerator#generateArchive(java.lang.Class)
    */
   @Override
   public Archive<?> generateArchive(Class<?> testCase)
   {
      Validate.notNull(testCase, "TestCase must be specified");
      List<Archive<?>> moduleArchives = DeploymentAppenders.getArchives();
      
      Archive<?> userArchive = generator.generateArchive(testCase);
      
      EnterpriseArchive fullDeployment = Archives.create("test.ear", EnterpriseArchive.class)
                  .addModule(userArchive);  
      
      for(Archive<?> moduleArchive : moduleArchives )
      {
         if(WebArchive.class.isInstance(moduleArchive))
         {
            fullDeployment.addModule(moduleArchive);
         } 
         else 
         {
            fullDeployment.addLibrary(moduleArchive);
         }
      }
      return fullDeployment;
   }
}
