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

   // TestRunner to used for testing
   private TestRunner mockTestRunner;

   private TestClassLoader testClassLoader;

   public interface TestClassLoader
   {
      Class<?> loadTestClass(String className) throws ClassNotFoundException;
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
      return runTestMethodInternal(className, methodName);
   }

   public InputStream runTestMethodEmbedded(String className, String methodName)
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
      currentCall.set(className + methodName);
      TestResult result = new TestResult();
      try
      {
         TestRunner runner = mockTestRunner;
         if (runner == null)
         {
            runner = TestRunners.getTestRunner(getClass().getClassLoader());
         }

         log.debugf("Load test class: %s", className);
         Class<?> testClass = testClassLoader.loadTestClass(className);
         log.debugf("Test class loaded from: %s", testClass.getClassLoader());

         log.debugf("Execute: %s.%s", testClass, methodName);
         result = runner.execute(testClass, methodName);
      }
      catch (Throwable th)
      {
         result.setStatus(Status.FAILED);
         result.setEnd(System.currentTimeMillis());
         result.setThrowable(th);
      }
      finally
      {
         log.debugf("Result: %s", result);
         if (result.getStatus() == Status.FAILED)
            log.errorf(result.getThrowable(), "Failed: %s.%s", className, methodName);
      }
      return result;
   }

   @Override
   public void send(Command<?> command)
   {
      Notification notification = new Notification("arquillian-command", this, integer.incrementAndGet(),
            currentCall.get());
      notification.setUserData(Serializer.toByteArray(command));
      sendNotification(notification);
   }

   @Override
   public Command<?> receive()
   {
      return events.remove(currentCall.get());
   }

   @Override
   public void push(String eventId, byte[] command)
   {
      events.put(eventId, Serializer.toObject(Command.class, command));
   }

   /*
    * Internal Helpers for Test
    */
   /**
    * @return the currentCall
    */
   protected String getCurrentCall()
   {
      return currentCall.get();
   }

   protected void setCurrentCall(String current)
   {
      currentCall.set(current);
   }
   
   void setExposedTestRunnerForTest(TestRunner mockTestRunner)
   {
      this.mockTestRunner = mockTestRunner;
   }
}
