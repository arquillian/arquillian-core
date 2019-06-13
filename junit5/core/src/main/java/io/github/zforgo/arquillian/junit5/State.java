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
package io.github.zforgo.arquillian.junit5;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;

/**
 * State
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
//TODO merge to common
public class State {
    /*
     * @HACK
     * JUnit Hack:
     * In JUnit a Exception is thrown and verified/swallowed if @Test(expected) is set. We need to transfer this
     * Exception back to the client so the client side can throw it again. This to avoid a incontainer working but failing
     * on client side due to no Exception thrown.
     */
    // Cleaned up in JUnitTestRunner
    private static ThreadLocal<Throwable> caughtTestException = new ThreadLocal<Throwable>();

    private static ThreadLocal<Throwable> caughtExceptionAfterJunit = new ThreadLocal<Throwable>();

    /*
     * Keep track of previous BeforeSuite initialization exceptions
     */
    private static ThreadLocal<Throwable> caughtInitializationException = new ThreadLocal<Throwable>();

    /*
     * @HACK
     * Eclipse hack:
     * When running multiple TestCases, Eclipse will create a new runner for each of them.
     * This results in that AfterSuite is call pr TestCase, but BeforeSuite only on the first created instance.
     * A instance of all TestCases are created before the first one is started, so we keep track of which one
     * was the last one created. The last one created is the only one allowed to call AfterSuite.
     */
    private static ThreadLocal<Integer> lastCreatedRunner = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return new Integer(0);
        }
    };

    /*
     * @HACK
     * Eclipse hack:
     * See above. We need to track constructor invocations when running in Eclipse, but run() invocations in
     * Surefire due to how groups are handled in Surefire.
     */
    private static boolean runningInEclipse = false;
    private static ThreadLocal<TestRunnerAdaptor> deployableTest = new ThreadLocal<TestRunnerAdaptor>();

    static {
        try {
            State.class.getClassLoader().loadClass("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner");
            runningInEclipse = true;
        } catch (Exception e) {
            runningInEclipse = false;
        }
    }

    public static boolean isRunningInEclipse() {
        return runningInEclipse;
    }

    public static boolean isNotRunningInEclipse() {
        return !runningInEclipse;
    }

    static void runnerStarted() {
        lastCreatedRunner.set(lastCreatedRunner.get() + 1);
    }

    static Integer runnerFinished() {
        Integer currentCount = lastCreatedRunner.get() - 1;
        lastCreatedRunner.set(currentCount);
        return currentCount;
    }

    static Integer runnerCurrent() {
        return lastCreatedRunner.get();
    }

    static boolean isLastRunner() {
        return runnerCurrent() == 0;
    }

    static void testAdaptor(TestRunnerAdaptor adaptor) {
        deployableTest.set(adaptor);
    }

    static boolean hasTestAdaptor() {
        return getTestAdaptor() != null;
    }

    static TestRunnerAdaptor getTestAdaptor() {
        return deployableTest.get();
    }

    static void caughtInitializationException(Throwable throwable) {
        caughtInitializationException.set(throwable);
    }

    static boolean hasInitializationException() {
        return getInitializationException() != null;
    }

    static Throwable getInitializationException() {
        return caughtInitializationException.get();
    }

    public static void caughtTestException(Throwable throwable) {
        if (throwable == null) {
            caughtTestException.remove();
        } else {
            caughtTestException.set(throwable);
        }
    }

    public static boolean hasTestException() {
        return getTestException() != null;
    }

    public static Throwable getTestException() {
        return caughtTestException.get();
    }

    public static Throwable caughtExceptionAfterJunit() {
        return caughtExceptionAfterJunit.get();
    }

    public static void caughtExceptionAfterJunit(Throwable afterException) {
        if (afterException == null) {
            caughtExceptionAfterJunit.remove();
        } else {
            caughtExceptionAfterJunit.set(afterException);
        }
    }

    static void clean() {
        lastCreatedRunner.remove();
        deployableTest.remove();
        caughtInitializationException.remove();
        caughtExceptionAfterJunit.remove();
    }
}
