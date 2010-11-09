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
package org.jboss.arquillian.impl.client.deployment;

import java.io.File;

import junit.framework.Assert;

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
import org.junit.Ignore;
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
@Ignore // not working
@RunWith(MockitoJUnitRunner.class)
public class ArchiveDeploymentExporterTestCase extends AbstractManagerTestBase
{
   private static final String ARCHIVE_NAME = "test.jar";
   private static final String EXPORT_PATH = "target/";
   
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(ArchiveDeploymentExporter.class);
   }
   
   @Mock
   private DeployableContainer<?> deployableContainer;
   
   @Mock 
   private DeploymentDescription deployment;

   @Test 
   public void shouldHandleNoArchiveInContext() throws Exception 
   {
      fire(new BeforeDeploy(deployableContainer, deployment));
      
      fileShouldExist(false);
   }

   @Test 
   public void shouldHandleNoConfigurationInContext() throws Exception 
   {
      fire(new BeforeDeploy(deployableContainer, deployment));
      
      fileShouldExist(false);
   }

   @Test
   public void shouldNotExportIfDeploymentExportPathNotSet() throws Exception 
   {
      //context.add(Archive.class, ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME).addClass(getClass()));
      bind(ApplicationScoped.class, Configuration.class, new Configuration());
      
      fire(new BeforeDeploy(deployableContainer, deployment));
      
      fileShouldExist(false);
   }
   
   @Test
   public void shouldBeExportedWhenDeploymentExportPathIsSet() throws Exception 
   {
      Configuration configuration = new Configuration();
      configuration.setDeploymentExportPath(EXPORT_PATH);

      bind(ApplicationScoped.class, Configuration.class, configuration);
      
      fire(new BeforeDeploy(deployableContainer, deployment));
      
      fileShouldExist(true);
   }

   private void fileShouldExist(boolean bol) 
   {
      File file = new File(EXPORT_PATH + getClass().getName() + "_" + ARCHIVE_NAME);
      
      Assert.assertEquals("File exists", bol, file.exists());
      
      file.delete();
   }
}
