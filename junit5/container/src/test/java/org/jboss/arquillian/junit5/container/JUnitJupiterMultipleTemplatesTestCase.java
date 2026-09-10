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
import org.jboss.arquillian.junit5.container.fixtures.ClassWithArquillianExtensionAndTwoTemplateTests;
import org.jboss.arquillian.junit5.extension.RunModeEvent;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Verifies that the run once bookkeeping is kept per test template, so that two templates in the same class are each
 * dispatched to the container once and their results do not leak into one another.
 */
public class JUnitJupiterMultipleTemplatesTestCase extends JUnitTestBaseClass {

    private static final String EXPECTED_DETAIL_MESSAGE = "expected: <3.14> but was: <2.71>";
    private static final Class<?> FIXTURE_CLASS = ClassWithArquillianExtensionAndTwoTemplateTests.class;
    private static final String TEST_METHOD_ID_FORMAT = "[engine:junit-jupiter]"
            + "/[class:" + FIXTURE_CLASS.getName() + "]"
            + "/[test-template:%s(java.lang.String)]"
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
    public void shouldKeepResultsOfTemplatesApart() throws Exception {
        // given only the second invocation of the second template fails in the container
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        doAnswer(invocation -> {
            TestMethodExecutor executor = invocation.getArgument(0);
            if ("firstParameterizedTest".equals(executor.getMethodName())) {
                return TestResult.passed();
            }
            return TestResult.failed(new IdentifiedTestException(
                    Map.of(uniqueId("secondParameterizedTest", 2), new AssertionError(EXPECTED_DETAIL_MESSAGE))));
        }).when(adaptor).test(any(TestMethodExecutor.class));

        // when
        TestExecutionSummary result = run(adaptor, FIXTURE_CLASS);

        // then all three invocations of the first template pass, only the second invocation of the second fails
        Assertions.assertEquals(5, result.getTestsSucceededCount());
        Assertions.assertEquals(1, result.getTestsFailedCount());
        TestExecutionSummary.Failure failure = result.getFailures().get(0);
        Assertions.assertTrue(failure.getTestIdentifier().getUniqueId()
                        .equals(uniqueId("secondParameterizedTest", 2)),
                "Expected the 2nd invocation of secondParameterizedTest to be the failed one, but was "
                        + failure.getTestIdentifier().getUniqueId());
        // Each template is dispatched to the container exactly once, so twice in total
        verify(adaptor, times(2)).test(any(TestMethodExecutor.class));
    }

    private static String uniqueId(String methodName, int invocation) {
        return String.format(TEST_METHOD_ID_FORMAT, methodName, invocation);
    }
}
