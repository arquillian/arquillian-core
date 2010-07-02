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
package org.jboss.arquillian.protocol.jmx;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.logging.Logger;

/**
 * JMXMethodExecutor
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JMXMethodExecutor implements ContainerMethodExecutor
{
   // Provide logging
   private static Logger log = Logger.getLogger(JMXMethodExecutor.class);
   
   @Override
   public TestResult invoke(TestMethodExecutor testMethodExecutor)
   {
      if(testMethodExecutor == null) 
         throw new IllegalArgumentException("TestMethodExecutor null");
      
      String testClass = testMethodExecutor.getInstance().getClass().getName();
      String testMethod = testMethodExecutor.getMethod().getName();

      TestResult result = null;
      try 
      {
         MBeanServer mbeanServer = findOrCreateMBeanServer();
         ObjectName objectName = new ObjectName(JMXTestRunnerMBean.OBJECT_NAME);
         JMXTestRunnerMBean testRunner = getMBeanProxy(mbeanServer, objectName, JMXTestRunnerMBean.class);

         // Invoke the remote test method
         InputStream resultStream = testRunner.runTestMethodRemote(testClass, testMethod);
         
         // Unmarshall the TestResult
         ObjectInputStream ois = new ObjectInputStream(resultStream);
         result = (TestResult)ois.readObject();
      }
      catch (final Throwable e) 
      {
         result = new TestResult(Status.FAILED);
         result.setThrowable(e);
      }
      finally
      {
         result.setEnd(System.currentTimeMillis());
      }
      return result;
   }

   private <T> T getMBeanProxy(MBeanServer mbeanServer, ObjectName name, Class<T> interf)
   {
      return (T)MBeanServerInvocationHandler.newProxyInstance(mbeanServer, name, interf, false);
   }

   private MBeanServer findOrCreateMBeanServer()
   {
      MBeanServer mbeanServer = null;

      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         log.warn("Multiple MBeanServer instances: " + serverArr);

      if (serverArr.size() > 0)
      {
         mbeanServer = serverArr.get(0);
         log.debug("Found MBeanServer: " + mbeanServer.getDefaultDomain());
      }

      if (mbeanServer == null)
      {
         log.debug("No MBeanServer, create one ...");
         mbeanServer = MBeanServerFactory.createMBeanServer();
      }

      return mbeanServer;
   }
}