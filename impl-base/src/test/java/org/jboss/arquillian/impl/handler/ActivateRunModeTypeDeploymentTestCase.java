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

import junit.framework.Assert;

import org.jboss.arquillian.api.RunMode;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.impl.ApplicationArchiveDeploymentGenerator;
import org.jboss.arquillian.impl.ClientDeploymentGenerator;
import org.jboss.arquillian.impl.DeploymentGenerator;
import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * ActivateRunModeTypeLocalDeploymentTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivateRunModeTypeDeploymentTestCase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Test
   public void shouldExportApplicationArchiveDeploymentGeneratorIfLocalMode() throws Exception 
   {
      verifyExportAndType(TestWithRunModeLocal.class, ApplicationArchiveDeploymentGenerator.class);
   }

   @Test
   public void shouldExportClientDeploymentGeneratorIfRemoteMode() throws Exception 
   {
      verifyExportAndType(TestWithRunModeRemote.class, ClientDeploymentGenerator.class);
   }
   
   @Test
   public void shouldExportClientDeploymentGeneratorIfNoModeSet() throws Exception 
   {
      verifyExportAndType(TestWithRunModeNone.class, ClientDeploymentGenerator.class);
   }

   private void verifyExportAndType(Class<?> testCaseClass, Class<?> deploymentGeneratorClass) throws Exception 
   {
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      
      ActivateRunModeTypeDeployment handler = new ActivateRunModeTypeDeployment();
      handler.callback(context, new BeforeClass(testCaseClass));
      
      Assert.assertNotNull(
            "Should have exported a " + DeploymentGenerator.class,
            context.get(DeploymentGenerator.class));
      
      Assert.assertEquals(
            "Verify that the correct " + DeploymentGenerator.class.getName() + " was exported", 
            deploymentGeneratorClass,
            context.get(DeploymentGenerator.class).getClass());
   }

   
   @RunMode(RunModeType.LOCAL)
   private static class TestWithRunModeLocal { }

   @RunMode(RunModeType.REMOTE)
   private static class TestWithRunModeRemote { }

   private static class TestWithRunModeNone { }
}
