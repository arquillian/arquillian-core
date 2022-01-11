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
package org.jboss.arquillian.junit.container;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.jboss.arquillian.junit.event.AfterRules;
import org.jboss.arquillian.junit.event.BeforeRules;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;

/**
 * JUnitTestBaseClass
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JUnitTestBaseClass {
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

    public static void wasCalled(Cycle cycle) throws Throwable {
        if (callbackCount.containsKey(cycle)) {
            callbackCount.put(cycle, callbackCount.get(cycle) + 1);
        } else {
            throw new RuntimeException("Unknown callback: " + cycle);
        }
        if (callbackException.containsKey(cycle)) {
            throw callbackException.get(cycle);
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
        doAnswer(new ExecuteLifecycle()).when(adaptor).fireCustomLifecycle(isA(BeforeRules.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).fireCustomLifecycle(isA(AfterRules.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor)
            .before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor)
            .after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new TestExecuteLifecycle(TestResult.passed())).when(adaptor).test(any(TestMethodExecutor.class));
    }

    public void assertCycle(int count, Cycle... cycles) {
        for (Cycle cycle : cycles) {
            Assert.assertEquals("Verify " + cycle + " called N times",
                count, (int) callbackCount.get(cycle));
        }
    }

    protected Result run(TestRunnerAdaptor adaptor, Class<?>... classes) throws Exception {
        return run(adaptor, null, classes);
    }

    protected Result run(TestRunnerAdaptor adaptor, RunListener listener, Class<?>... classes)
        throws Exception {
        try {
            setAdaptor(adaptor);
            JUnitCore core = new JUnitCore();
            if (listener != null) {
                core.addListener(listener);
            }

            return core.run(classes);
        } finally {
            setAdaptor(null);
        }
    }

    // force set the TestRunnerAdaptor to use
    private void setAdaptor(TestRunnerAdaptor adaptor) throws Exception {
        Method method = TestRunnerAdaptorBuilder.class.getMethod("set", TestRunnerAdaptor.class);
        method.setAccessible(true);
        method.invoke(null, adaptor);
    }

    public enum Cycle

    {
        BEFORE_CLASS_RULE, BEFORE_RULE, BEFORE_CLASS, BEFORE, TEST, AFTER, AFTER_CLASS, AFTER_RULE, AFTER_CLASS_RULE;
    }

    /*
     * Mockito Answers for invoking the LifeCycle callbacks.
     */
    public static class ExecuteLifecycle implements Answer<Object> {
        @Override
        public Object answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
            for (Object argument : invocation.getArguments()) {
                if (argument instanceof LifecycleMethodExecutor) {
                    ((LifecycleMethodExecutor) argument).invoke();
                } else if (argument instanceof TestMethodExecutor) {
                    ((TestMethodExecutor) argument).invoke();
                } else if (argument instanceof TestLifecycleEvent) {
                    ((TestLifecycleEvent) argument).getExecutor().invoke();
                }
            }
            return null;
        }
    }

    public static class TestExecuteLifecycle extends ExecuteLifecycle {
        private TestResult result;

        public TestExecuteLifecycle(TestResult result) {
            this.result = result;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            super.answer(invocation);
            return result;
        }
    }
}
