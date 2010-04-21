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
package org.jboss.arquillian.testng;

import org.jboss.arquillian.spi.AuxiliaryArchiveAppender;
import org.jboss.arquillian.spi.TestRunner;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.internal.AnnotationTypeEnum;
import org.testng.log.TextFormatter;
import org.testng.log4testng.Logger;
import org.testng.remote.RemoteTestNG;
import org.testng.reporters.XMLUtils;
import org.testng.util.RetryAnalyzerCount;
import org.testng.v6.TestPlan;
import org.testng.xml.XmlSuite;

import com.thoughtworks.qdox.Searcher;

/**
 * TestNGDeploymentAppender
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestNGDeploymentAppender implements AuxiliaryArchiveAppender
{

   public Archive<?> createAuxiliaryArchive()
   {
      return Archives.create("arquillian-testng.jar", JavaArchive.class)
               .addPackages(
                     true, 
                     Test.class.getPackage(),
                     AnnotationTypeEnum.class.getPackage(),
                     RetryAnalyzerCount.class.getPackage(),
                     TextFormatter.class.getPackage(),
                     Logger.class.getPackage(),
                     TestPlan.class.getPackage(),
                     XmlSuite.class.getPackage(),
                     Searcher.class.getPackage(),
                     Maps.class.getPackage(),
                     //IJUnitTestRunner.class.getPackage(),
                     RemoteTestNG.class.getPackage(),
                     XMLUtils.class.getPackage(),
                     Package.getPackage("org.jboss.arquillian.testng"))
               .addPackage(TestNG.class.getPackage())
               .addServiceProvider(
                     TestRunner.class, 
                     TestNGTestRunner.class);
   }
}
