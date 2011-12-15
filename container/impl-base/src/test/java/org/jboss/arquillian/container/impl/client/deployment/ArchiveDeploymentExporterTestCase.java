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
package org.jboss.arquillian.container.impl.client.deployment;

import java.io.File;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Assert;
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
public class ArchiveDeploymentExporterTestCase extends AbstractContainerTestBase
{
   /**
    * 
    */
   private static final String ARQUILLIAN_DEPLOYMENT_EXPORT_PATH = "arquillian.deploymentExportPath";

   private static final String TARGET_NAME = "test.jar";

   private static final String DEPLOYMENT_NAME = "test.jar";
   
   private static final String ARCHIVE_NAME = "test.jar";

   private static final String EXPORT_PATH = "target/";

   @Override
   protected void addExtensions(List<Class<?>> extensions)
   {
      extensions.add(ArchiveDeploymentExporter.class);
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
   public void shouldExportIfExportPathSystemPropertyIsSet() throws Exception
   {
      System.setProperty(ARQUILLIAN_DEPLOYMENT_EXPORT_PATH, EXPORT_PATH);
      try
      {
         bind(ApplicationScoped.class, ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class));
         
         fire(new BeforeDeploy(deployableContainer, deployment));
   
         fileShouldExist(true);
      }
      finally
      {
         System.setProperty(ARQUILLIAN_DEPLOYMENT_EXPORT_PATH, "");
      }
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
