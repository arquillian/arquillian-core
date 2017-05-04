/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.After;
import org.junit.Assert;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * JUnitTestBaseClass
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestNGTestBaseClass {
    /*
     * Setup / Clear the static callback info.
     */
    private static Map<Cycle, Integer> callbackCount = new HashMap<Cycle, Integer>();
    private static Map<Cycle, Throwable> callbackException = new HashMap<Cycle, Throwable>();

    static {
        for (Cycle tmp : Cycle.values()) {
            callbackCount.put(tmp, 0);
        }
    }

    public static void throwException(Cycle cycle, Throwable exception) {
        callbackException.put(cycle, exception);
    }

    public static void wasCalled(Cycle cycle) throws Throwable {
        System.out.println("called: " + cycle);
        if (callbackCount.containsKey(cycle)) {
            callbackCount.put(cycle, callbackCount.get(cycle) + 1);
        } else {
            throw new RuntimeException("Unknown callback: " + cycle);
        }
        if (callbackException.containsKey(cycle)) {
            throw callbackException.remove(cycle);
        }
    }

    @After
    public void clearCallbacks() {
        callbackCount.clear();
        for (Cycle tmp : Cycle.values()) {
            callbackCount.put(tmp, 0);
        }
        callbackException.clear();
    }

    /*
     * Internal Helpers
     */
    protected void executeAllLifeCycles(TestRunnerAdaptor adaptor) throws Exception {
        doAnswer(new ExecuteLifecycle(Cycle.BEFORE_SUITE)).when(adaptor).beforeSuite();
        doAnswer(new ExecuteLifecycle(Cycle.AFTER_SUITE)).when(adaptor).afterSuite();
        doAnswer(new ExecuteLifecycle(Cycle.BEFORE_CLASS)).when(adaptor)
            .beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle(Cycle.AFTER_CLASS)).when(adaptor)
            .afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle(Cycle.BEFORE)).when(adaptor)
            .before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle(Cycle.AFTER)).when(adaptor)
            .after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new TestExecuteLifecycle(new TestResult(Status.PASSED))).when(adaptor)
            .test(any(TestMethodExecutor.class));
    }

    public void assertCycle(int count, Cycle... cycles) {
        for (Cycle cycle : cycles) {
            Assert.assertEquals("Verify " + cycle + " called N times",
                count, (int) callbackCount.get(cycle));
        }
    }

    protected TestListenerAdapter run(TestRunnerAdaptor adaptor, Class<?>... classes)
        throws Exception {
        return run(null, adaptor, classes);
    }

    protected TestListenerAdapter run(String[] groups, TestRunnerAdaptor adaptor, Class<?>... classes)
        throws Exception {
        try {
            setAdaptor(adaptor);

            TestListenerAdapter listener = new TestListenerAdapter();
            TestNG runner = new TestNG(false);
            runner.addListener(listener);
            runner.setXmlSuites(Collections.singletonList(createSuite(groups, classes)));

            runner.run();
            return listener;
        } finally {
            setAdaptor(null);
        }
    }

    protected boolean wasSuccessful(TestListenerAdapter adapter) {
        return adapter.getFailedTests().size() == 0 && adapter.getSkippedTests().size() == 0;
    }

    private XmlSuite createSuite(String[] groups, Class<?>... classes) {
        XmlSuite suite = new XmlSuite();
        suite.setName("Arquillian - TEST");

        suite.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
        XmlTest test = new XmlTest(suite);
        if (groups != null) {
            test.setIncludedGroups(Arrays.asList(groups));
        }
        test.setName("Arquillian - TEST");
        List<XmlClass> testClasses = new ArrayList<XmlClass>();
        for (Class<?> clazz : classes) {
            XmlClass testClass = new XmlClass(clazz);
            testClasses.add(testClass);
        }
        test.setXmlClasses(testClasses);
        return suite;
    }

    // force set the TestRunnerAdaptor to use
    private void setAdaptor(TestRunnerAdaptor adaptor) throws Exception {
        Method method = TestRunnerAdaptorBuilder.class.getMethod("set", TestRunnerAdaptor.class);
        method.setAccessible(true);
        method.invoke(null, adaptor);
    }

    public static enum Cycle

    {
        BEFORE_SUITE, BEFORE_CLASS, BEFORE, TEST, AFTER, AFTER_CLASS, AFTER_SUITE
    }

    /*
     * Mockito Answers for invoking the LifeCycle callbacks.
     */
    public static class ExecuteLifecycle implements Answer<Object> {
        private Cycle cycle;

        public ExecuteLifecycle(Cycle cycle) {
            this.cycle = cycle;
        }

        @Override
        public Object answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
            wasCalled(cycle);
            for (Object argument : invocation.getArguments()) {
                if (argument instanceof LifecycleMethodExecutor) {
                    ((LifecycleMethodExecutor) argument).invoke();
                } else if (argument instanceof TestMethodExecutor) {
                    ((TestMethodExecutor) argument).invoke();
                }
            }
            return null;
        }
    }

    public static class TestExecuteLifecycle extends ExecuteLifecycle {
        private TestResult result;

        public TestExecuteLifecycle(TestResult result) {
            super(Cycle.TEST);
            this.result = result;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            super.answer(invocation);
            return result;
        }
    }
}
