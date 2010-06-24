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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.TestRunner;
import org.jboss.arquillian.spi.util.TestRunners;
import org.jboss.logging.Logger;

/**
 * An MBean to run test methods in container.
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JMXTestRunner implements JMXTestRunnerMBean
{
   // Provide logging
   private static Logger log = Logger.getLogger(JMXTestRunner.class);

   public static ObjectName register(MBeanServer mbeanServer) throws JMException
   {
      ObjectName oname = new ObjectName(JMXTestRunnerMBean.OBJECT_NAME);
      mbeanServer.registerMBean(new JMXTestRunner(), oname);
      log.debug("JMXTestRunner registered: " + oname);
      return oname;
   }

   public static void unregister(MBeanServer mbeanServer) throws JMException
   {
      ObjectName oname = new ObjectName(JMXTestRunnerMBean.OBJECT_NAME);
      if (mbeanServer.isRegistered(oname))
      {
         mbeanServer.unregisterMBean(oname);
         log.debug("JMXTestRunner unregistered: " + oname);
      }
   }

   @Override
   public TestResult runTestMethodLocal(String className, String methodName)
   {
      return runTestMethodInternal(className, methodName);
   }

   @Override
   public InputStream runTestMethodRemote(String className, String methodName)
   {
      TestResult result = runTestMethodInternal(className, methodName);

      // Marshall the TestResult
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(result);
         oos.close();

         return new ByteArrayInputStream(baos.toByteArray());
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot marshall response", ex);
      }
   }

   private TestResult runTestMethodInternal(String className, String methodName)
   {
      try
      {
         ClassLoader classLoader = getClass().getClassLoader();
         Class<?> testClass = classLoader.loadClass(className);

         TestRunner runner = TestRunners.getTestRunner(classLoader);

         TestResult testResult = runner.execute(testClass, methodName);
         return testResult;
      }
      catch (Throwable th)
      {
         return new TestResult(Status.FAILED, th);
      }
   }
}
