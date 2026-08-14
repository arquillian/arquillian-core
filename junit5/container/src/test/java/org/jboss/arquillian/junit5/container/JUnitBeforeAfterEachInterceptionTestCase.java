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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

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
 * Verifies that {@code @BeforeEach} and {@code @AfterEach} methods are correctly
 * invoked or skipped depending on whether the lifecycle event observer invokes the
 * executor — simulating local-container vs remote-container scenarios.
 */
public class JUnitBeforeAfterEachInterceptionTestCase extends JUnitTestBaseClass {

    /**
     * Simulates a local container where {@code isRunAsClient=false} (testable deployment)
     * but the adaptor still invokes the executor (because {@code isLocalContainer=true}).
     * This is the scenario that was broken before the fix — the interceptor's guard
     * {@code IS_INSIDE_ARQUILLIAN || isRunAsClient} would skip directly without
     * delegating to the adaptor.
     */
    @Test
    public void shouldRunBeforeAfterEachForLocalContainerWithTestableDeployment() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);
        // Make isRunAsClient() return false — simulates a testable deployment
        // where RunModeEventHandler sets runAsClient=false.
        doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof RunModeEvent) {
                ((RunModeEvent) event).setRunAsClient(false);
            }
            return null;
        }).when(adaptor).fireCustomLifecycle(any(TestLifecycleEvent.class));

        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionWithBeforeAfterEach.class);

        Assertions.assertEquals(1, result.getTestsSucceededCount());
        Assertions.assertEquals(0, result.getTestsFailedCount());
        assertCycle(1, Cycle.BEFORE, Cycle.TEST, Cycle.AFTER);
    }

    /**
     * Simulates a remote container where {@code isRunAsClient=false} and the adaptor
     * does NOT invoke the executor (the container will run the methods itself).
     * {@code @BeforeEach} and {@code @AfterEach} methods must be skipped on the client side.
     */
    @Test
    public void shouldSkipBeforeAfterEachForRemoteContainerWithTestableDeployment() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        // Make isRunAsClient() return false
        doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof RunModeEvent) {
                ((RunModeEvent) event).setRunAsClient(false);
            }
            return null;
        }).when(adaptor).fireCustomLifecycle(any(TestLifecycleEvent.class));
        // Only invoke the executor for beforeClass/afterClass and test — NOT for before/after.
        // This simulates a remote container where ClientBeforeAfterLifecycleEventExecuter
        // does not invoke the executor because isRunAsClient=false and isLocalContainer=false.
        doAnswer(new ExecuteLifecycle()).when(adaptor).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new TestExecuteLifecycle(TestResult.passed())).when(adaptor).test(any(TestMethodExecutor.class));

        TestExecutionSummary result = run(adaptor, ClassWithArquillianExtensionWithBeforeAfterEach.class);

        Assertions.assertEquals(1, result.getTestsSucceededCount());
        Assertions.assertEquals(0, result.getTestsFailedCount());
        assertCycle(0, Cycle.BEFORE, Cycle.AFTER);
        assertCycle(1, Cycle.TEST);
    }
}
