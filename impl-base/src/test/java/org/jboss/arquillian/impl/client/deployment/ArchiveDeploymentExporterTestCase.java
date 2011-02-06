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
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Before;
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
public class ArchiveDeploymentExporterTestCase extends AbstractManagerTestBase
{
   private static final String TARGET_NAME = "test.jar";

   private static final String DEPLOYMENT_NAME = "test.jar";
   
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

   @Before
   public void createDeployment()
   {
      Archive<?> archive = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME).addClass(getClass());

      deployment = new DeploymentDescription(DEPLOYMENT_NAME, archive);
      deployment.setTarget(new TargetDescription(TARGET_NAME));
      deployment.setTestableArchive(archive);
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
      bind(ApplicationScoped.class, ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class));

      fire(new BeforeDeploy(deployableContainer, deployment));

      fileShouldExist(false);
   }

   @Test
   public void shouldNotExportedIfDeploymentIsNotArchive() throws Exception
   {
      bind(ApplicationScoped.class, ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class).engine()
            .deploymentExportPath(EXPORT_PATH));

      deployment = new DeploymentDescription(DEPLOYMENT_NAME, Descriptors.create(WebAppDescriptor.class));
      deployment.setTarget(new TargetDescription(TARGET_NAME));
      
      fire(new BeforeDeploy(deployableContainer, deployment));

      fileShouldExist(false);
   }

   @Test
   public void shouldBeExportedWhenDeploymentExportPathIsSet() throws Exception
   {
      bind(ApplicationScoped.class, ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class).engine()
            .deploymentExportPath(EXPORT_PATH));

      fire(new BeforeDeploy(deployableContainer, deployment));

      fileShouldExist(true);
   }

   private void fileShouldExist(boolean bol)
   {
      File file = new File(EXPORT_PATH + TARGET_NAME + "_" + DEPLOYMENT_NAME + "_" + ARCHIVE_NAME);

      Assert.assertEquals("File exists", bol, file.exists());

      file.delete();
   }
}
