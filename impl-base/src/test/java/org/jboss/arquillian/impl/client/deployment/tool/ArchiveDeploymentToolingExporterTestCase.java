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
package org.jboss.arquillian.impl.client.deployment.tool;

import java.io.File;

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.event.container.BeforeDeploy;
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
public class ArchiveDeploymentToolingExporterTestCase extends AbstractManagerTestBase
{
   private static final String EXPORT_FOLDER = "target/";
   
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(ArchiveDeploymentToolingExporter.class);
   }
   
   @Mock
   private DeployableContainer<?> deployableContainer;
   
   @Mock 
   private DeploymentDescription deployment;

   @Test
   public void shouldThrowIllegalStateExceptionOnMissingDeploymentGenerator() throws Exception
   {
      System.setProperty(ArchiveDeploymentToolingExporter.ARQUILLIAN_TOOLING_DEPLOYMENT_FOLDER, EXPORT_FOLDER);
      
      //context.add(Archive.class, ShrinkWrap.create(JavaArchive.class, "test.jar"));

      fire(new BeforeDeploy(deployableContainer, deployment));
      
      Assert.assertTrue(new File(EXPORT_FOLDER + getClass().getName() + ".xml").exists());
   }

}
