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
package org.jboss.arquillian.junit.testspi;

import org.jboss.arquillian.junit.JUnitIntegrationTestCase;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.shrinkwrap.api.Archive;

/**
 * TestDeployableContainer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestDeployableContainer implements DeployableContainer
{
   private int numberOfTimesDeployed = 0;
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#setup(org.jboss.arquillian.spi.Context, org.jboss.arquillian.spi.Configuration)
    */
   public void setup(Context context, Configuration configuration)
   {
      JUnitIntegrationTestCase.wasCalled("setup");
   }


   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#start(org.jboss.arquillian.spi.Context)    */
   public void start(Context context) throws LifecycleException
   {
      JUnitIntegrationTestCase.wasCalled("start");
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#stop(org.jboss.arquillian.spi.Context)
    */
   public void stop(Context context) throws LifecycleException
   {
      JUnitIntegrationTestCase.wasCalled("stop");
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#deploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   public ContainerMethodExecutor deploy(Context context, Archive<?> archive) throws DeploymentException
   {
      numberOfTimesDeployed++;
      JUnitIntegrationTestCase.wasCalled("deploy");
      if(numberOfTimesDeployed == 1) 
      {
         throw new RuntimeException("deploy");
      }

      return new ContainerMethodExecutor()
      {
         public TestResult invoke(TestMethodExecutor testMethodExecutor)
         {
            TestResult result = new TestResult();
            try
            {
               testMethodExecutor.invoke();
               result.setStatus(Status.PASSED);
            }
            catch (Throwable e) 
            {
               result.setStatus(Status.FAILED);
               result.setThrowable(e);
            }
            return result;
         }
      };
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#undeploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      JUnitIntegrationTestCase.wasCalled("undeploy");
      if(numberOfTimesDeployed == 1)
      {
         throw new RuntimeException("undeploy");
      }
   }
}
