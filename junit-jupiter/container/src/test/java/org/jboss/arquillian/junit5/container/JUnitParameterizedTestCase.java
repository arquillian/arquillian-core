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

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class JUnitParameterizedTestCase extends JUnitTestBaseClass {

    @Override
    protected void executeAllLifeCycles(TestRunnerAdaptor adaptor) throws Exception {
        doAnswer(new ExecuteLifecycle()).when(adaptor).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).before(any(Object.class), any(Method.class),
                any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).after(any(Object.class), any(Method.class),
                any(LifecycleMethodExecutor.class));
        doAnswer(new TestExecuteLifecycle()).when(adaptor).test(any(TestMethodExecutor.class));
    }

    @Test
    public void shouldReportFailures() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionAndParameterizedTest.class);

        // then
        Assertions.assertEquals(2, result.getTestsSucceededCount());
        Assertions.assertEquals(2, result.getTestsFailedCount());
        Assertions.assertEquals(0, result.getTestsSkippedCount());
        assertCycle(0, Cycle.BEFORE_RULE, Cycle.AFTER_RULE, Cycle.BEFORE_CLASS_RULE, Cycle.AFTER_CLASS_RULE);
        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS);
        assertCycle(4, Cycle.BEFORE, Cycle.TEST, Cycle.AFTER);
    }

    public static class TestExecuteLifecycle extends ExecuteLifecycle {

        @Override
        public Object answer(InvocationOnMock invocation) {
            try {
                super.answer(invocation);
            } catch (Throwable t) {
                return TestResult.failed(t);
            }
            return TestResult.passed();
        }
    }
}
