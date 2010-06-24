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
package org.jboss.arquillian.jmx;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.arquillian.protocol.jmx.JMXTestRunner;
import org.jboss.arquillian.protocol.jmx.JMXTestRunnerMBean;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.junit.Test;


/**
 * Test the {@link JMXTestRunner}
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JMXTestRunnerTestCase
{
   @Test
   public void testJMXTestRunner() throws Throwable
   {
      MBeanServer mbeanServer = getMBeanServer();
      ObjectName oname = JMXTestRunner.register(mbeanServer);
      
      try
      {
         JMXTestRunnerMBean testRunner = getMBeanProxy(mbeanServer, oname, JMXTestRunnerMBean.class);
         TestResult result = testRunner.runTestMethodLocal(DummyTestCase.class.getName(), "testMethod");
         
         assertNotNull("TestResult not null", result);
         assertNotNull("Status not null", result.getStatus());
         if (result.getStatus() == Status.FAILED)
            throw result.getThrowable();
      }
      finally
      {
         mbeanServer.unregisterMBean(oname);
      }
   }

   private MBeanServer getMBeanServer()
   {
      ArrayList<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer(null);
      MBeanServer mbeanServer = (mbeanServers.size() < 1 ? MBeanServerFactory.createMBeanServer() : mbeanServers.get(0));
      return mbeanServer;
   }

   private <T> T getMBeanProxy(MBeanServer mbeanServer, ObjectName name, Class<T> interf)
   {
      return (T)MBeanServerInvocationHandler.newProxyInstance(mbeanServer, name, interf, false);
   }
}
