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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.mockito.invocation.InvocationOnMock;

@Disabled("https://github.com/arquillian/arquillian-core/issues/771")
public class JUnitJupiterParameterizedTestCase extends JUnitTestBaseClass {

    @Override
    protected void executeAllLifeCycles(TestRunnerAdaptor adaptor) throws Exception {
        doAnswer(new ExecuteLifecycle()).when(adaptor).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).before(any(Object.class),
                any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).after(any(Object.class),
                any(Method.class), any(LifecycleMethodExecutor.class));
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
    public void shouldExecuteParameterizedTestOnceInContainer() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // when
        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionAndParameterizedTest.class);

        // then
        Assertions.assertEquals(2, result.getTestsSucceededCount());
        Assertions.assertEquals(2, result.getTestsFailedCount());
        Assertions.assertEquals(0, result.getTestsSkippedCount());
        verify(adaptor, times(1)).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(1)).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(4)).before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(4)).after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        verify(adaptor, times(2)).test(any(TestMethodExecutor.class));
    }

    public static class TestExecuteLifecycle extends ExecuteLifecycle {

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            try {
                super.answer(invocation);
            } catch (Throwable t) {
                return TestResult.failed(t);
            }
            return TestResult.passed();
        }
    }
}
