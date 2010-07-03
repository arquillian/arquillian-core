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
package org.jboss.arquillian.impl.handler;

import java.io.File;

import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * ArchiveDeploymentToolingExporterTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ArchiveDeploymentToolingExporterTestCase
{
   private static final String EXPORT_FOLDER = "target/";
   
   @Mock
   private ServiceLoader serviceLoader;
   
   @Test
   public void shouldThrowIllegalStateExceptionOnMissingDeploymentGenerator() throws Exception
   {
      System.setProperty(ArchiveDeploymentToolingExporter.ARQUILLIAN_TOOLING_DEPLOYMENT_FOLDER, EXPORT_FOLDER);
      
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      context.add(Archive.class, ShrinkWrap.create("test.jar", JavaArchive.class));
      
      ArchiveDeploymentToolingExporter handler = new ArchiveDeploymentToolingExporter();
      handler.callback(context, new ClassEvent(getClass()));
      
      Assert.assertTrue(new File(EXPORT_FOLDER + getClass().getName() + ".xml").exists());
   }

}
