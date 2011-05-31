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

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.protocol.jmx.JMXProtocolConfiguration.ExecutionType;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;

/**
 * JMXMethodExecutor
 *
 * @author thomas.diesler@jboss.com
 */
public class JMXMethodExecutor implements ContainerMethodExecutor
{
   private final MBeanServerConnection mbeanServer;
   private final ExecutionType executionType;
   private final CommandCallback callback;

   public JMXMethodExecutor(MBeanServerConnection mbeanServer, ExecutionType executionType, CommandCallback callbac)
   {
      this.mbeanServer = mbeanServer;
      this.executionType = executionType;
      this.callback = callbac;
   }

   public TestResult invoke(TestMethodExecutor testMethodExecutor)
   {
      if (testMethodExecutor == null)
         throw new IllegalArgumentException("TestMethodExecutor null");

      String testClass = testMethodExecutor.getInstance().getClass().getName();
      String testMethod = testMethodExecutor.getMethod().getName();

      NotificationListener commandListener = null;
      ObjectName objectName = null; 
      TestResult result = null;
      try
      {
         objectName = new ObjectName(JMXTestRunnerMBean.OBJECT_NAME);
         commandListener = new CallbackNotificationListener(objectName);
         mbeanServer.addNotificationListener(objectName, commandListener, null, null);

         JMXTestRunnerMBean testRunner = getMBeanProxy(objectName, JMXTestRunnerMBean.class);
         if (executionType == ExecutionType.REMOTE)
         {
            result = testRunner.runTestMethodRemote(testClass, testMethod);
         }
         else
         {
            InputStream resultStream = testRunner.runTestMethodEmbedded(testClass, testMethod);
            ObjectInputStream ois = new ObjectInputStream(resultStream);
            result = (TestResult)ois.readObject();
         }
      }
      catch (final Throwable e)
      {
         result = new TestResult(Status.FAILED);
         result.setThrowable(e);
      }
      finally
      {
         result.setEnd(System.currentTimeMillis());
         if(objectName != null && commandListener != null)
         {
            try
            {
               mbeanServer.removeNotificationListener(objectName, commandListener);
            }
            catch (Exception e) 
            {
               e.printStackTrace();
            }
         }
      }
      return result;
   }

   private <T> T getMBeanProxy(ObjectName name, Class<T> interf)
   {
      return (T)MBeanServerInvocationHandler.newProxyInstance(mbeanServer, name, interf, false);
   }
   
   private class CallbackNotificationListener implements NotificationListener
   {
      private ObjectName serviceName;
      
      public CallbackNotificationListener(ObjectName serviceName)
      {
         this.serviceName = serviceName;
      }
      
      @Override
      public void handleNotification(Notification notification, Object handback)
      {
         String eventMessage = notification.getMessage();
         Command<?> command = Serializer.toObject(Command.class, (byte[])notification.getUserData()); 
         callback.fired(command);
         
         try
         {
            mbeanServer.invoke(
                  serviceName, 
                  "push", 
                  new Object[] {eventMessage, Serializer.toByteArray(command)}, 
                  new String[] {String.class.getName(), byte[].class.getName()});
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not return command result for command " + command, e);
         }
      }      
   }
}