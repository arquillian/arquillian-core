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

import org.jboss.arquillian.testng.container.TestNGDeploymentAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.junit.Assert;
import org.junit.Test;

/**
 * TestNGDeploymentAppenderTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestNGDeploymentAppenderTestCase
{

   @Test
   public void shouldGenerateDependencies() throws Exception 
   {
      Archive<?> archive = new TestNGDeploymentAppender().createAuxiliaryArchive();

      Assert.assertTrue(
            "Should have added TestRunner SPI",
            archive.contains(ArchivePaths.create("/META-INF/services/org.jboss.arquillian.core.spi.LoadableExtension")));
      
      Assert.assertTrue(
            "Should have added TestRunner Impl",
            archive.contains(ArchivePaths.create("/org/jboss/arquillian/testng/container/TestNGTestRunner.class")));

      System.out.println(archive.toString(true));      

   }
}
