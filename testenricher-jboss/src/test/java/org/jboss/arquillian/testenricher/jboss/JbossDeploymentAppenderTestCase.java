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

import junit.framework.Assert;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Paths;
import org.junit.Test;

/**
 * JbossDeploymentAppenderTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JbossDeploymentAppenderTestCase
{
   
   @Test
   public void shouldGenerateDependencies() throws Exception {

      Archive<?> archive = new JbossDeploymentAppender().createArchive();
      System.out.println(archive.toString(true));
      
      Assert.assertTrue(
            "Should have added TestEnricher SPI", 
            archive.contains(Paths.create("/META-INF/services/org.jboss.arquillian.spi.TestEnricher")));

      Assert.assertTrue(
            "Should have added TestEnricher EJB impl", 
            archive.contains(Paths.create("/org/jboss/arquillian/testenricher/jboss/EJBInjectionEnricher.class")));

      Assert.assertTrue(
            "Should have added TestEnricher Resource impl", 
            archive.contains(Paths.create("/org/jboss/arquillian/testenricher/jboss/ResourceInjectionEnricher.class")));

      Assert.assertTrue(
            "Should have added TestEnricher CDI impl", 
            archive.contains(Paths.create("/org/jboss/arquillian/testenricher/jboss/CDIInjectionEnricher.class")));

      Assert.assertTrue(
            "Should have added TestEnricher Impl dep", 
            archive.contains(Paths.create("/org/jboss/arquillian/testenricher/jboss/SecurityActions.class")));
   }
}
