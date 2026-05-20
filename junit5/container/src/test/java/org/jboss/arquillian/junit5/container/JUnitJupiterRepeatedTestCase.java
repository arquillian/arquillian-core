/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.jboss.arquillian.junit5.extension.RunModeEvent;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Verifies that @RepeatedTest executes exactly the specified number of repetitions
 * and not the square of that number. See <a href="https://github.com/arquillian/arquillian-core/issues/771">GH issue</a>.
 *
 * @author Radoslav Husar
 */
public class JUnitJupiterRepeatedTestCase extends JUnitTestBaseClass {

    @Override
    protected void executeAllLifeCycles(TestRunnerAdaptor adaptor) throws Exception {
        doAnswer(new ExecuteLifecycle()).when(adaptor).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new TestExecuteLifecycle()).when(adaptor).test(any(TestMethodExecutor.class));
        doAnswer(invocation -> {
            TestLifecycleEvent event = invocation.getArgument(0);
            if (event instanceof RunModeEvent) {
                ((RunModeEvent) event).setRunAsClient(false);
            }
            return null;
        }).when(adaptor).fireCustomLifecycle(any(TestLifecycleEvent.class));
    }

    @Test
    public void shouldExecuteRepeatedTestExactlyThreeTimes() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionAndRepeatedTest.class);

        // then — @RepeatedTest(3) must invoke SPI lifecycle exactly 3 times, not 9
        Assertions.assertEquals(0, result.getTestsFailedCount());
        verify(adaptor, times(1)).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(1)).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(3)).before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(3)).after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(1)).test(any(TestMethodExecutor.class));
    }

    @Test
    public void shouldReportFailuresForAllRepetitions() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        doAnswer(invocation -> TestResult.failed(new AssertionError("expected failure")))
            .when(adaptor).test(any(TestMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionAndRepeatedTest.class);

        // then — all 3 repetitions must report as failed
        Assertions.assertEquals(3, result.getTestsFailedCount());
        verify(adaptor, times(1)).test(any(TestMethodExecutor.class));
    }

    public static class TestExecuteLifecycle extends ExecuteLifecycle {

        @Override
        public Object answer(org.mockito.invocation.InvocationOnMock invocation) {
            try {
                super.answer(invocation);
            } catch (Throwable t) {
                return TestResult.failed(t);
            }
            return TestResult.passed();
        }
    }
}
