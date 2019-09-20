/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import org.jboss.arquillian.protocol.jmx.test.JMXTestTestRunner;
import org.jboss.arquillian.protocol.jmx.test.MockTestRunner;
import org.jboss.arquillian.protocol.jmx.test.TestCommandCallback;
import org.jboss.arquillian.protocol.jmx.test.TestIntegerCommand;
import org.jboss.arquillian.protocol.jmx.test.TestStringCommand;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test the {@link JMXTestRunner}
 *
 * @author thomas.diesler@jboss.com
 */
public class JMXTestRunnerTestCase {

    @Test
    public void testJMXTestRunner() throws Throwable {
        MBeanServer mbeanServer = getMBeanServer();
        JMXTestRunner jmxTestRunner = new JMXTestRunner(null);
        ObjectName oname = jmxTestRunner.registerMBean(mbeanServer);

        try {
            JMXTestRunnerMBean testRunner = getMBeanProxy(mbeanServer, oname, JMXTestRunnerMBean.class);
            TestResult result = Serializer.toObject(TestResult.class,
                testRunner.runTestMethod(DummyTestCase.class.getName(), "testMethod", new HashMap<String, String>()));

            assertNotNull("TestResult not null", result);
            assertNotNull("Status not null", result.getStatus());

            if (result.getStatus() == Status.FAILED) {
                throw result.getThrowable();
            }
        } finally {
            mbeanServer.unregisterMBean(oname);
        }
    }

    @Test // backwards compatible
    public void shouldInvokeOldRunMethodIfOldMethodIsOverridden() throws Throwable {
        MBeanServer mbeanServer = getMBeanServer();
        final AtomicInteger count = new AtomicInteger();
        JMXTestRunner jmxTestRunner = new JMXTestRunner(null) {
            @Override
            public byte[] runTestMethod(String className, String methodName) {
                count.incrementAndGet();
                return super.runTestMethod(className, methodName);
            }
        };
        ObjectName oname = jmxTestRunner.registerMBean(mbeanServer);

        try {
            JMXTestRunnerMBean testRunner = getMBeanProxy(mbeanServer, oname, JMXTestRunnerMBean.class);
            TestResult result = Serializer.toObject(TestResult.class,
                testRunner.runTestMethod(DummyTestCase.class.getName(), "testMethod", new HashMap<String, String>()));

            assertNotNull("TestResult not null", result);
            assertNotNull("Status not null", result.getStatus());

            assertEquals("Old runTestMethod should have been called", 1, count.get());

            if (result.getStatus() == Status.FAILED) {
                throw result.getThrowable();
            }
        } finally {
            mbeanServer.unregisterMBean(oname);
        }
    }

    @Test // backwards compatible
    public void shouldInvokeNewRunMethodIfNewMethodIsOverridden() throws Throwable {
        MBeanServer mbeanServer = getMBeanServer();
        final AtomicInteger count = new AtomicInteger();
        JMXTestRunner jmxTestRunner = new JMXTestRunner(null) {
            @Override
            public byte[] runTestMethod(String className, String methodName, Map<String, String> props) {
                count.incrementAndGet();
                return super.runTestMethod(className, methodName, props);
            }
        };
        ObjectName oname = jmxTestRunner.registerMBean(mbeanServer);

        try {
            JMXTestRunnerMBean testRunner = getMBeanProxy(mbeanServer, oname, JMXTestRunnerMBean.class);
            TestResult result = Serializer.toObject(TestResult.class,
                testRunner.runTestMethod(DummyTestCase.class.getName(), "testMethod", new HashMap<String, String>()));

            assertNotNull("TestResult not null", result);
            assertNotNull("Status not null", result.getStatus());

            assertEquals("New runTestMethod should have been called", 1, count.get());

            if (result.getStatus() == Status.FAILED) {
                throw result.getThrowable();
            }
        } finally {
            mbeanServer.unregisterMBean(oname);
        }
    }

    @Test
    public void shouldBeAbleToSendReceiveCommands() throws Throwable {
        Object[] results = new Object[] {"Success", 100};
        MockTestRunner.add(TestResult.passed());
        MockTestRunner.add(new TestStringCommand());
        MockTestRunner.add(new TestIntegerCommand());

        MBeanServer mbeanServer = getMBeanServer();
        JMXTestRunner jmxTestRunner = new JMXTestTestRunner(null);

        jmxTestRunner.setExposedTestRunnerForTest(new MockTestRunner());
        ObjectName oname = jmxTestRunner.registerMBean(mbeanServer);

        try {
            JMXMethodExecutor executor = new JMXMethodExecutor(mbeanServer, new TestCommandCallback(results));

            TestResult result = executor.invoke(new TestMethodExecutor() {
                @Override
                public void invoke(Object... parameters) throws Throwable {
                }

                @Override
                public String getMethodName() {
                    return getMethod().getName();
                }

                @Override
                public Method getMethod() {
                    return testMethod();
                }

                @Override
                public Object getInstance() {
                    return JMXTestRunnerTestCase.this;
                }
            });

            assertNotNull("TestResult not null", result);
            assertNotNull("Status not null", result.getStatus());
            if (result.getStatus() == Status.FAILED) {
                throw result.getThrowable();
            }

            for (int i = 0; i < results.length; i++) {
                Assert.assertEquals(
                    "Should have returned command",
                    results[i],
                    MockTestRunner.commandResults.get(i));
            }
        } finally {
            mbeanServer.unregisterMBean(oname);
        }
    }

    private MBeanServer getMBeanServer() {
        ArrayList<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer(null);
        MBeanServer mbeanServer =
            (mbeanServers.size() < 1 ? MBeanServerFactory.createMBeanServer() : mbeanServers.get(0));
        return mbeanServer;
    }

    private <T> T getMBeanProxy(MBeanServer mbeanServer, ObjectName name, Class<T> interf) {
        return MBeanServerInvocationHandler.newProxyInstance(mbeanServer, name, interf, false);
    }

    private Method testMethod() {
        try {
            return DummyTestCase.class.getMethod("testMethod");
        } catch (Exception e) {
            throw new RuntimeException("Could not lookup testMethod, check " + DummyTestCase.class, e);
        }
    }
}
