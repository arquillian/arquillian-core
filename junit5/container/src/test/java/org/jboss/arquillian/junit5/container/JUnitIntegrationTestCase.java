/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit5.container;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.jboss.arquillian.junit5.container.fixtures.ClassWithArquillianExtensionAndTwoBeforeEach;
import org.jboss.arquillian.junit5.container.fixtures.ClassWithArquillianExtensionWithExtensions;
import org.jboss.arquillian.junit5.container.fixtures.ExampleSuite;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.opentest4j.TestAbortedException;

/**
 * @author Johannes Beck
 * @author Radoslav Husar
 */
public class JUnitIntegrationTestCase extends JUnitTestBaseClass {

    @Test
    public void shouldExecuteExtensions() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionWithExtensions.class);

        // then
        Assertions.assertEquals(1, result.getTestsSucceededCount());
        Assertions.assertEquals(0, result.getTestsFailedCount());
        Assertions.assertEquals(0, result.getTestsSkippedCount());
        assertCycle(1, Cycle.BEFORE_RULE, Cycle.BEFORE_CLASS, Cycle.BEFORE, Cycle.TEST, Cycle.AFTER, Cycle.AFTER_CLASS,
            Cycle.AFTER_RULE, Cycle.BEFORE_CLASS_RULE, Cycle.AFTER_CLASS_RULE);
    }

    /**
     * Verifies that {@code TestRunnerAdaptor.before()} and {@code TestRunnerAdaptor.after()} are each called
     * exactly once per test method and not duplicated.
     * See <a href="https://github.com/arquillian/arquillian-core/issues/745">GH issue</a>.
     */
    @Test
    public void shouldCallBeforeAndAfterExactlyOncePerTest() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionWithExtensions.class);

        // then — single @Test method, so before()/after() must be called exactly once
        Assertions.assertEquals(1, result.getTestsSucceededCount());
        Assertions.assertEquals(0, result.getTestsFailedCount());

        verify(adaptor, times(1)).before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(1)).after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
    }

    /**
     * Verifies that {@code TestRunnerAdaptor.before()} and {@code TestRunnerAdaptor.after()} are each called exactly
     * once per test method also with multiple {@code @BeforeEach} methods and no {@code @AfterEach} method.
     * See <a href="https://github.com/arquillian/arquillian-core/issues/745">GH issue</a>.
     */
    @Test
    public void shouldCallBeforeAndAfterExactlyOncePerTestWithMultipleBeforeEach() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionAndTwoBeforeEach.class);

        // then
        Assertions.assertEquals(1, result.getTestsSucceededCount());
        Assertions.assertEquals(0, result.getTestsFailedCount());
        assertCycle(2, Cycle.BEFORE);
        assertCycle(1, Cycle.TEST);

        verify(adaptor, times(1)).before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(1)).after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
    }

    /**
     * Verifies that the {@code @BeforeEach} and {@code @AfterEach} methods are skipped when
     * {@code TestRunnerAdaptor.before()} does not invoke the {@code LifecycleMethodExecutor}, e.g. due to a
     * {@code DONT_EXECUTE} decision.
     */
    @Test
    public void shouldSkipLifecycleMethodsWhenBeforeDoesNotInvokeExecutor() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionWithExtensions.class);

        // then
        Assertions.assertEquals(0, result.getTestsFailedCount());
        assertCycle(0, Cycle.BEFORE, Cycle.AFTER);
    }

    /**
     * Verifies that a test is reported as aborted when {@code TestRunnerAdaptor.before()} throws an assumption failure,
     * e.g. from a {@code ServerSetupTask} (ARQ-2238).
     */
    @Test
    public void shouldAbortTestWhenBeforeThrowsAssumptionFailure() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        doThrow(new TestAbortedException("assumption failed")).when(adaptor)
            .before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionWithExtensions.class);

        // then
        Assertions.assertEquals(1, result.getTestsAbortedCount());
        Assertions.assertEquals(0, result.getTestsFailedCount());
        assertCycle(0, Cycle.BEFORE, Cycle.TEST);
        verify(adaptor, times(1)).after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
    }

    @Test
    public void runJunit5Suite() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // when
        TestExecutionSummary result = runSuite(adaptor, ExampleSuite.class);

        // then
        Assertions.assertEquals(4, result.getTestsFoundCount());
        Assertions.assertEquals(1, result.getTestsSucceededCount());
        Assertions.assertEquals(1, result.getTestsFailedCount());
        Assertions.assertEquals(1, result.getTestsSkippedCount());
        Assertions.assertEquals(1, result.getTestsAbortedCount());
    }
}
