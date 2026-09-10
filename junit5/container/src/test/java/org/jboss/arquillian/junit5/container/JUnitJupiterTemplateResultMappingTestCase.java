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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.jboss.arquillian.junit5.IdentifiedTestException;
import org.jboss.arquillian.junit5.container.fixtures.ClassWithArquillianExtensionAndRepeatedTest;
import org.jboss.arquillian.junit5.extension.RunModeEvent;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.opentest4j.TestAbortedException;

/**
 * Verifies that a container-side failure which cannot be attributed to an individual template invocation is still
 * reported, rather than being silently mapped to a passing result.
 */
public class JUnitJupiterTemplateResultMappingTestCase extends JUnitTestBaseClass {

    private static final Class<?> FIXTURE_CLASS = ClassWithArquillianExtensionAndRepeatedTest.class;
    private static final String TEMPLATE_ID = "[engine:junit-jupiter]"
            + "/[class:" + FIXTURE_CLASS.getName() + "]"
            + "/[test-template:repeatedTest()]";

    @Override
    protected void executeAllLifeCycles(TestRunnerAdaptor adaptor) throws Exception {
        doAnswer(invocation -> {
            TestLifecycleEvent event = invocation.getArgument(0);
            if (event instanceof RunModeEvent) {
                ((RunModeEvent) event).setRunAsClient(false);
            }
            return null;
        }).when(adaptor).fireCustomLifecycle(any(TestLifecycleEvent.class));
    }

    @Test
    public void shouldReportTemplateSkippedInContainer() throws Exception {
        // given the whole template - not an individual invocation - was skipped in the container, e.g. by a container
        // evaluated ExecutionCondition that does not hold there
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        doAnswer(invocation -> TestResult.failed(new IdentifiedTestException(
                Map.of(TEMPLATE_ID, new TestAbortedException("Disabled in the container")))))
                .when(adaptor).test(any(TestMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, FIXTURE_CLASS);

        // then no repetition may be reported as having passed
        Assertions.assertEquals(0, result.getTestsSucceededCount());
        Assertions.assertEquals(3, result.getTestsFailedCount());
        verify(adaptor).test(any(TestMethodExecutor.class));
    }

    @Test
    public void shouldNotSwallowUnattributableFailureAlongsideInvocationFailure() throws Exception {
        // given the container reported a failure for one invocation plus a failure that belongs to no invocation
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        doAnswer(invocation -> TestResult.failed(new IdentifiedTestException(Map.of(
                TEMPLATE_ID + "/[test-template-invocation:#2]", new AssertionError("Run 2 failed"),
                TEMPLATE_ID, new AssertionError("The template itself failed")))))
                .when(adaptor).test(any(TestMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, FIXTURE_CLASS);

        // then the repetitions the container did not report on must not be turned green either
        Assertions.assertEquals(0, result.getTestsSucceededCount());
        Assertions.assertEquals(3, result.getTestsFailedCount());
        verify(adaptor).test(any(TestMethodExecutor.class));
    }

    @Test
    public void shouldContactContainerOnlyOnceWhenTheRoundTripThrows() throws Exception {
        // given a container round trip that throws instead of returning a TestResult, e.g. an unreachable container
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        final IllegalStateException containerFailure = new IllegalStateException("Container is not reachable");
        doAnswer(invocation -> {
            throw containerFailure;
        }).when(adaptor).test(any(TestMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, FIXTURE_CLASS);

        // then every repetition fails, but the container is contacted exactly once rather than once per repetition
        Assertions.assertEquals(0, result.getTestsSucceededCount());
        Assertions.assertEquals(3, result.getTestsFailedCount());
        result.getFailures().forEach(failure -> Assertions.assertSame(containerFailure, failure.getException()));
        verify(adaptor, times(1)).test(any(TestMethodExecutor.class));
    }
}
