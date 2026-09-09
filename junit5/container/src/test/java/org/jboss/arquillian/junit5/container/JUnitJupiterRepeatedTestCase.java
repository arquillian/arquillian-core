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

/**
 * Verifies that @RepeatedTest executes exactly the specified number of repetitions
 * and not the square of that number. See <a href="https://github.com/arquillian/arquillian-core/issues/771">GH issue</a>.
 *
 * @author Radoslav Husar
 */
public class JUnitJupiterRepeatedTestCase extends JUnitTestBaseClass {

    private static final String EXPECTED_DETAIL_MESSAGE = "expected: <3.14> but was: <2.71>";
    private static final AssertionError ASSERTION_ERROR = new AssertionError(EXPECTED_DETAIL_MESSAGE);
    private static final Class<?> FIXTURE_CLASS = ClassWithArquillianExtensionAndRepeatedTest.class;
    private static final String TEST_METHOD_ID_FORMAT = "[engine:junit-jupiter]"
            + "/[class:" + FIXTURE_CLASS.getName() + "]"
            + "/[test-template:repeatedTest()]"
            + "/[test-template-invocation:#%d]";

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
    public void shouldPassAllRepetitions() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        doAnswer(invocation -> TestResult.passed())
                .when(adaptor).test(any(TestMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, FIXTURE_CLASS);

        // then
        Assertions.assertEquals(3, result.getTestsSucceededCount());
        Assertions.assertEquals(0, result.getTestsFailedCount());
        verify(adaptor).test(any(TestMethodExecutor.class));
    }

    @Test
    public void shouldFailAllRepetitions() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        // Run 1: ClassWithArquillianExtensionAndRepeatedTest.repeatedTest:30 failed:
        // expected: <3.14> but was: <2.71>
        // Run 2: ClassWithArquillianExtensionAndRepeatedTest.repeatedTest:30 failed:
        // expected: <3.14> but was: <2.71>
        // Run 3: ClassWithArquillianExtensionAndRepeatedTest.repeatedTest:30 failed:
        // expected: <3.14> but was: <2.71>
        doAnswer(invocation -> TestResult.failed(new IdentifiedTestException(Map.of(
                uniqueId(1), ASSERTION_ERROR,
                uniqueId(2), ASSERTION_ERROR,
                uniqueId(3), ASSERTION_ERROR))))
                .when(adaptor).test(any(TestMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, FIXTURE_CLASS);

        // then
        Assertions.assertEquals(0, result.getTestsSucceededCount());
        Assertions.assertEquals(3, result.getTestsFailedCount());
        for (int i = 0; i < result.getFailures().size();) {
            TestExecutionSummary.Failure failure = result.getFailures().get(i++); // post-increment
            Assertions.assertEquals("repetition " + i + " of " + result.getTestsFailedCount(),
                    failure.getTestIdentifier().getDisplayName());
            Assertions.assertTrue(failure.getException().getMessage().contains(EXPECTED_DETAIL_MESSAGE),
                    "Run " + i + ": expected failure message");
        }
        verify(adaptor).test(any(TestMethodExecutor.class));
    }

    @Test
    public void shouldFailOnlySecondRepetition() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        // Run 1: Pass
        // Run 2: ClassWithArquillianExtensionAndRepeatedTest.repeatedTest:30 failed:
        // expected: <3.14> but was: <2.71>
        // Run 3: Pass
        doAnswer(invocation -> TestResult
                .failed(new IdentifiedTestException(Map.of(uniqueId(2), ASSERTION_ERROR))))
                .when(adaptor).test(any(TestMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, FIXTURE_CLASS);

        // then
        Assertions.assertEquals(2, result.getTestsSucceededCount());
        Assertions.assertEquals(1, result.getTestsFailedCount());
        TestExecutionSummary.Failure failure = result.getFailures().get(0);
        Assertions.assertEquals("repetition 2 of 3", failure.getTestIdentifier().getDisplayName());
        Assertions.assertTrue(failure.getException().getMessage().contains(EXPECTED_DETAIL_MESSAGE),
                "Run 2: expected failure message");
        verify(adaptor).test(any(TestMethodExecutor.class));
    }

    private static String uniqueId(int repetition) {
        return String.format(TEST_METHOD_ID_FORMAT, repetition);
    }
}
