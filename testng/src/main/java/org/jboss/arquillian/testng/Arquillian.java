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

import java.lang.reflect.Method;

import org.jboss.arquillian.impl.DeployableTest;
import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

/**
 * Arquillian
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class Arquillian implements IHookable
{
   private static DeployableTest deployableTest = null;

   private Archive<?> archive = null;
   private ContainerMethodExecutor methodExecutor;
   
   @BeforeSuite
   public void createAndStartContainer() throws LifecycleException
   {
      if (deployableTest == null)
      {
         deployableTest = DeployableTestBuilder.build(null);
      }
      deployableTest.getContainerController().start();
   }

   @AfterSuite
   public void destroyAndStopContainer() throws LifecycleException
   {
      if (deployableTest == null)
      {
         return;
      }
      deployableTest.getContainerController().stop();
   }

   @BeforeClass
   public void createAndDeployArtifact() throws DeploymentException
   {
      archive = deployableTest.generateArchive(this.getClass());
      methodExecutor = deployableTest.getDeployer().deploy(archive);
   }

   @AfterClass
   public void destroyAndUndeployArtifact() throws DeploymentException
   {
      deployableTest.getDeployer().undeploy(archive);
   }

   public void run(final IHookCallBack callback, final ITestResult testResult)
   {
      TestResult result = methodExecutor.invoke(new TestMethodExecutor()
      {
         @Override
         public void invoke() throws Throwable
         {
            callback.runTestMethod(testResult);
         }
         
         @Override
         public Method getMethod()
         {
            return testResult.getMethod().getMethod();
         }
         
         @Override
         public Object getInstance()
         {
            return Arquillian.this;
         }
      });
      if(result.getThrowable() != null)
      {
         testResult.setThrowable(result.getThrowable());
      }
   }
}
