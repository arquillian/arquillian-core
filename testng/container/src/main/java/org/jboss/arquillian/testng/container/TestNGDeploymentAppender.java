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
package org.jboss.arquillian.testng.container;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * TestNGDeploymentAppender
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestNGDeploymentAppender extends CachedAuxilliaryArchiveAppender
{
   @Override
   protected Archive<?> buildArchive()
   {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-testng.jar")
               .addPackages(
                     true, 
                     // exclude com.sun.javadoc.Doclet loading, not in OpenJDK
                     Filters.exclude("/org/testng/junit/.*|/org/testng/eclipse/.*"), 
                     "org.testng",
                     "bsh",
                     "org.jboss.arquillian.testng")
               .addAsServiceProvider(
                     TestRunner.class, 
                     TestNGTestRunner.class);
   
      /* Attempt to add Guice if on classpath. TestNG 5.12 > use Guice */
      // exclude AOP Alliance reference, not provided as part of TestNG jar
      optionalPackages(
            archive, 
            Filters.exclude(".*/InterceptorStackCallback\\$InterceptedMethodInvocation.*"), 
            "com.google.inject");
         
      /* Attempt to add com.beust, internal TestNG package 5.14 > */
      optionalPackages(
            archive, 
            Filters.includeAll(), 
            "com.beust");

      return archive;
   }
   
   private void optionalPackages(JavaArchive jar, Filter<ArchivePath> filter, String... packages)
   {
      try
      {
         jar.addPackages(
               true, 
               filter,
               packages);
      }
      catch (Exception e) { /* optional packages NO-OP */  }
   }
}
