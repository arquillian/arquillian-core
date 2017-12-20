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
package org.jboss.arquillian.junit.container;

import java.util.Collections;
import java.util.List;
import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.junit.State;
import org.jboss.arquillian.test.spi.TestResult;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * JUnitTestRunner
 * <p>
 * A Implementation of the Arquillian TestRunner SPI for JUnit.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JUnitTestRunner implements TestRunner {
    /**
     * Overwrite to provide additional run listeners.
     */
    protected List<RunListener> getRunListeners() {
        return Collections.emptyList();
    }

    public TestResult execute(Class<?> testClass, String methodName) {
        TestResult testResult;
        ExpectedExceptionHolder exceptionHolder = new ExpectedExceptionHolder();
        try {
            JUnitCore runner = new JUnitCore();

            runner.addListener(exceptionHolder);

            for (RunListener listener : getRunListeners())
                runner.addListener(listener);

            Result result = runner.run(Request.method(testClass, methodName));

            if (result.getFailureCount() > 0) {
                testResult = TestResult.failed(exceptionHolder.getException());
            } else if (result.getIgnoreCount() > 0) {
                testResult = TestResult.skipped(); // Will this ever happen incontainer?
            } else {
                testResult = TestResult.passed();
            }
            if (testResult.getThrowable() == null) {
                testResult.setThrowable(exceptionHolder.getException());
            }
        } catch (Throwable th) {
            testResult = TestResult.failed(th);
        }
        if (testResult.getThrowable() instanceof AssumptionViolatedException) {
            testResult = TestResult.skipped(testResult.getThrowable());
        }
        testResult.setEnd(System.currentTimeMillis());
        return testResult;
    }

    private class ExpectedExceptionHolder extends RunListener {
        private Throwable exception;

        public Throwable getException() {
            return exception;
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            // AssumptionViolatedException might not be Serializable. Recreate with only String message.
            exception = new AssumptionViolatedException(failure.getException().getMessage());
            exception.setStackTrace(failure.getException().getStackTrace());
            ;
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            if (exception != null) {
                // In case of multiple errors only keep the first exception
                return;
            }
            exception = State.getTestException();
            Test test = failure.getDescription().getAnnotation(Test.class);
            if (!(test != null && test.expected() != Test.None.class)) {
                // Not Expected Exception, and non thrown internally
                if (exception == null) {
                    exception = failure.getException();
                }
            }
        }

        @Override
        public void testFinished(Description description) throws Exception {
            Test test = description.getAnnotation(Test.class);
            if (test != null && test.expected() != Test.None.class) {
                if (exception == null) {
                    exception = State.getTestException();
                }
            }
            State.caughtTestException(null);
        }
    }
}
