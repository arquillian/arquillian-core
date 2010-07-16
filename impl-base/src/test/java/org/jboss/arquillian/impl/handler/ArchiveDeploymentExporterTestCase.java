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

import junit.framework.Assert;

import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Verify the behavior of the ArchiveDeploymentExporter 
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ArchiveDeploymentExporterTestCase
{
   private static final String ARCHIVE_NAME = "test.jar";
   private static final String EXPORT_PATH = "target/";
   
   @Mock
   private ServiceLoader serviceLoader;

   @Test
   public void shouldHandleNoArchiveInContext() throws Exception 
   {
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      
      ArchiveDeploymentExporter handler = new ArchiveDeploymentExporter();
      handler.callback(context, new BeforeClass(getClass()));
      
      fileShouldExist(false);
   }

   @Test
   public void shouldHandleNoConfigurationInContext() throws Exception 
   {
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      
      context.add(Archive.class, ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME).addClass(getClass()));
      
      ArchiveDeploymentExporter handler = new ArchiveDeploymentExporter();
      handler.callback(context, new BeforeClass(getClass()));
      
      fileShouldExist(false);
   }

   @Test
   public void shouldNotExportIfDeploymentExportPathNotSet() throws Exception 
   {
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      
      context.add(Archive.class, ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME).addClass(getClass()));
      context.add(Configuration.class,  new Configuration());
      
      ArchiveDeploymentExporter handler = new ArchiveDeploymentExporter();
      handler.callback(context, new BeforeClass(getClass()));
      
      fileShouldExist(false);
   }
   
   @Test
   public void shouldBeExportedWhenDeploymentExportPathIsSet() throws Exception 
   {
      Configuration configuration = new Configuration();
      configuration.setDeploymentExportPath(EXPORT_PATH);
      
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      
      context.add(Archive.class, ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME).addClass(getClass()));
      context.add(Configuration.class,  configuration);
      
      ArchiveDeploymentExporter handler = new ArchiveDeploymentExporter();
      handler.callback(context, new BeforeClass(getClass()));
      
      fileShouldExist(true);
   }

   private void fileShouldExist(boolean bol) 
   {
      File file = new File(EXPORT_PATH + getClass().getName() + "_" + ARCHIVE_NAME);
      
      Assert.assertEquals("File exists", bol, file.exists());
      
      file.delete();
   }
}
