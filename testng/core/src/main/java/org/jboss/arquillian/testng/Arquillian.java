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
package org.jboss.arquillian.testng;

import java.lang.reflect.Method;
import java.util.Stack;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.jboss.arquillian.test.spi.execution.SkippedTestExecutionException;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

/**
 * Arquillian
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Listeners(Arquillian.UpdateResultListener.class)
public abstract class Arquillian implements IHookable {
    public static final String ARQUILLIAN_DATA_PROVIDER = "ARQUILLIAN_DATA_PROVIDER";
    private static InheritableThreadLocal<TestRunnerAdaptor> deployableTest = new InheritableThreadLocal<TestRunnerAdaptor>();
    private static InheritableThreadLocal<Stack<Cycle>> cycleStack = new InheritableThreadLocal<Stack<Cycle>>() {
        protected java.util.Stack<Cycle> initialValue() {
            return new Stack<Cycle>();
        }

        ;
    };

    @BeforeSuite(groups = "arquillian", inheritGroups = true)
    public void arquillianBeforeSuite() throws Exception {
        if (deployableTest.get() == null) {
            TestRunnerAdaptor adaptor = TestRunnerAdaptorBuilder.build();
            adaptor.beforeSuite();
            deployableTest.set(adaptor); // don't set TestRunnerAdaptor if beforeSuite fails
            cycleStack.get().push(Cycle.BEFORE_SUITE);
        }
    }

    @AfterSuite(groups = "arquillian", inheritGroups = true, alwaysRun = true)
    public void arquillianAfterSuite() throws Exception {
        if (deployableTest.get() == null) {
            return; // beforeSuite failed
        }
        if (cycleStack.get().empty()) {
            return;
        }
        if (cycleStack.get().peek() != Cycle.BEFORE_SUITE) {
            return; // Arquillian lifecycle called out of order, expected " + Cycle.BEFORE_SUITE
        } else {
            cycleStack.get().pop();
        }
        deployableTest.get().afterSuite();
        deployableTest.get().shutdown();
        deployableTest.set(null);
        deployableTest.remove();
        cycleStack.set(null);
        cycleStack.remove();
    }

    @BeforeClass(groups = "arquillian", inheritGroups = true)
    public void arquillianBeforeClass() throws Exception {
        verifyTestRunnerAdaptorHasBeenSet();
        cycleStack.get().push(Cycle.BEFORE_CLASS);
        deployableTest.get().beforeClass(getClass(), LifecycleMethodExecutor.NO_OP);
    }

    @AfterClass(groups = "arquillian", inheritGroups = true, alwaysRun = true)
    public void arquillianAfterClass() throws Exception {
        if (cycleStack.get().empty()) {
            return;
        }
        if (cycleStack.get().peek() != Cycle.BEFORE_CLASS) {
            return; // Arquillian lifecycle called out of order, expected " + Cycle.BEFORE_CLASS
        } else {
            cycleStack.get().pop();
        }
        verifyTestRunnerAdaptorHasBeenSet();
        deployableTest.get().afterClass(getClass(), LifecycleMethodExecutor.NO_OP);
    }

    @BeforeMethod(groups = "arquillian", inheritGroups = true)
    public void arquillianBeforeTest(Method testMethod) throws Exception {
        verifyTestRunnerAdaptorHasBeenSet();
        cycleStack.get().push(Cycle.BEFORE);
        deployableTest.get().before(this, testMethod, LifecycleMethodExecutor.NO_OP);
    }

    @AfterMethod(groups = "arquillian", inheritGroups = true, alwaysRun = true)
    public void arquillianAfterTest(Method testMethod) throws Exception {
        if (cycleStack.get().empty()) {
            return;
        }
        if (cycleStack.get().peek() != Cycle.BEFORE) {
            return; // Arquillian lifecycle called out of order, expected " + Cycle.BEFORE_CLASS
        } else {
            cycleStack.get().pop();
        }
        verifyTestRunnerAdaptorHasBeenSet();
        deployableTest.get().after(this, testMethod, LifecycleMethodExecutor.NO_OP);
    }

    public void run(final IHookCallBack callback, final ITestResult testResult) {
        verifyTestRunnerAdaptorHasBeenSet();
        TestResult result;
        try {
            result = deployableTest.get().test(new TestMethodExecutor() {
                public void invoke(Object... parameters) throws Throwable {
               /*
                *  The parameters are stored in the InvocationHandler, so we can't set them on the test result directly.
                *  Copy the Arquillian found parameters to the InvocationHandlers parameters
                */
                    copyParameters(parameters, callback.getParameters());
                    callback.runTestMethod(testResult);

                    // Parameters can be contextual, so extract information
                    swapWithClassNames(callback.getParameters());
                    testResult.setParameters(callback.getParameters());
                    if (testResult.getThrowable() != null) {
                        throw testResult.getThrowable();
                    }
                }

                private void copyParameters(Object[] source, Object[] target) {
                    for (int i = 0; i < source.length; i++) {
                        if (source[i] != null) {
                            target[i] = source[i];
                        }
                    }
                }

                private void swapWithClassNames(Object[] source) {
                    // clear parameters. they can be contextual and might fail TestNG during the report writing.
                    for (int i = 0; source != null && i < source.length; i++) {
                        Object parameter = source[i];
                        if (parameter != null) {
                            source[i] = parameter.toString();
                        } else {
                            source[i] = "null";
                        }
                    }
                }

                public String getMethodName() {
                    return testResult.getMethod().getMethodName();
                }

                public Method getMethod() {
                    return testResult.getMethod().getMethod();
                }

                public Object getInstance() {
                    return Arquillian.this;
                }
            });
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                if (result.getStatus() == Status.SKIPPED) {
                    if (throwable instanceof SkippedTestExecutionException) {
                        result.setThrowable(new SkipException(throwable.getMessage()));
                    }
                }
                testResult.setThrowable(result.getThrowable());

                // setting status as failed.
                testResult.setStatus(2);
            }

            // calculate test end time. this is overwritten in the testng invoker..
            testResult.setEndMillis((result.getStart() - result.getEnd()) + testResult.getStartMillis());
        } catch (Exception e) {
            testResult.setThrowable(e);
        }
    }

    @DataProvider(name = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    public Object[][] arquillianArgumentProvider(Method method) {
        Object[][] values = new Object[1][method.getParameterTypes().length];

        if (deployableTest.get() == null) {
            return values;
        }

        Object[] parameterValues = new Object[method.getParameterTypes().length];
        values[0] = parameterValues;

        return values;
    }

    private void verifyTestRunnerAdaptorHasBeenSet() {
        if (deployableTest.get() == null) {
            throw new IllegalStateException("No TestRunnerAdaptor found, @BeforeSuite has not been called");
        }
    }

    private static enum Cycle

    {
        BEFORE_SUITE, BEFORE_CLASS, BEFORE, TEST, AFTER, AFTER_CLASS, AFTER_SUITE
    }

    public static final class UpdateResultListener implements IInvokedMethodListener {

        @Override
        public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
            if (method.isTestMethod() && testResult.getStatus() != ITestResult.SUCCESS) {
                State.caughtExceptionAfter(testResult.getThrowable());
            }
        }

        @Override
        public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        }
    }
}
