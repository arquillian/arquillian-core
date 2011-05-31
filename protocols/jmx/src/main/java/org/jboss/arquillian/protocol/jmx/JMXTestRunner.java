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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.util.TestRunners;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.logging.Logger;

/**
 * An MBean to run test methods in container.
 *
 * @author thomas.diesler@jboss.com
 */
public class JMXTestRunner extends NotificationBroadcasterSupport implements JMXTestRunnerMBean 
{
   // Provide logging
   private static Logger log = Logger.getLogger(JMXTestRunner.class);
  
   // package shared MBeanServer with JMXCommandService
   static MBeanServer localMBeanServer; 
   
   private ConcurrentHashMap<String, Command<?>> events;
   private ThreadLocal<String> currentCall;

   // Notification Sequence number
   private AtomicInteger integer = new AtomicInteger();

   // opens for setting TestRunner to use, used for testing
   private TestRunner exposedTestRunnerForTest;
   
   private TestClassLoader testClassLoader;
   
   public interface TestClassLoader
   {
       Class<?> loadTestClass(String className) throws ClassNotFoundException;
       ClassLoader getServiceClassLoader();
   }
   
   public JMXTestRunner(TestClassLoader classLoader)
   {
      this.testClassLoader = classLoader;
      
      // Initialize the default TestClassLoader
      if (testClassLoader == null)
      {
         testClassLoader = new TestClassLoader()
         {
            public Class<?> loadTestClass(String className) throws ClassNotFoundException
            {
               ClassLoader classLoader = JMXTestRunner.class.getClassLoader();
               return classLoader.loadClass(className);
            }

            @Override
            public ClassLoader getServiceClassLoader() {
                ClassLoader classLoader = JMXTestRunner.class.getClassLoader();
                return classLoader;
            }
         };
      }
      events = new ConcurrentHashMap<String, Command<?>>();
      currentCall = new ThreadLocal<String>();
   }

   public ObjectName registerMBean(MBeanServer mbeanServer) throws JMException
   {
      ObjectName oname = new ObjectName(JMXTestRunnerMBean.OBJECT_NAME);
      mbeanServer.registerMBean(this, oname);
      log.debug("JMXTestRunner registered: " + oname);
      localMBeanServer = mbeanServer;
      return oname;
   }

   public void unregisterMBean(MBeanServer mbeanServer) throws JMException
   {
      ObjectName oname = new ObjectName(JMXTestRunnerMBean.OBJECT_NAME);
      if (mbeanServer.isRegistered(oname))
      {
         mbeanServer.unregisterMBean(oname);
         log.debug("JMXTestRunner unregistered: " + oname);
      }
      localMBeanServer = null;
   }

   public TestResult runTestMethodRemote(String className, String methodName)
   {
      currentCall.set(className+methodName);
      return runTestMethodInternal(className, methodName);
   }

   public InputStream runTestMethodEmbedded(String className, String methodName)
   {
      currentCall.set(className+methodName);
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
         TestRunner runner = exposedTestRunnerForTest;
         if(runner == null)
         {
            runner = TestRunners.getTestRunner(testClassLoader.getServiceClassLoader());
         }
         Class<?> testClass = testClassLoader.loadTestClass(className);
         
         TestResult testResult = runner.execute(testClass, methodName);
         return testResult;
      }
      catch (Throwable th)
      {
         return new TestResult(Status.FAILED, th);
      }
   }

   public void setExposedTestRunnerForTest(TestRunner exposedTestRunnerForTest)
   {
      this.exposedTestRunnerForTest = exposedTestRunnerForTest;
   }

   @Override
   public void send(Command<?> command)
   {
      Notification notification = new Notification("arquillian-command", this, integer.incrementAndGet(), currentCall.get());
      notification.setUserData(Serializer.toByteArray(command));
      sendNotification(notification);
   }
   
   @Override
   public Command<?> receive()
   {
      return events.get(currentCall.get());
   }

   @Override
   public void push(String eventId, byte[] command)
   {
      events.put(eventId, Serializer.toObject(Command.class, command));
   }
}
