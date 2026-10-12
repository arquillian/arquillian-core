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

import static org.jboss.arquillian.junit5.JUnitJupiterTestClassLifecycleManager.MANAGER_SCOPE_PROPERTY;
import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.BEFORE_CLASS;
import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.BEFORE_SUITE;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.junit5.container.fixtures.ParallelFixtureA;
import org.jboss.arquillian.junit5.container.fixtures.ParallelFixtureB;
import org.jboss.arquillian.junit5.container.fixtures.ParallelRendezvous;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * End-to-end check that class scope does what it exists for: letting Jupiter run whole test classes at the same
 * time, each against its own Arquillian manager.
 *
 * <p>The fixtures meet at a {@link java.util.concurrent.CyclicBarrier}, so the run only succeeds if the two classes
 * are genuinely in flight at the same moment. That is what makes this a test rather than a hopeful sleep - and
 * {@link #shouldFailWhenClassesDoNotActuallyOverlap()} demonstrates the mechanism noticing a sequential run.</p>
 */
public class JUnitJupiterParallelClassExecutionTestCase extends JUnitTestBaseClass {

    /**
     * A fixed pool, because the default strategy sizes itself from the available processors and would hand a
     * single-core machine a parallelism of one - turning this into a sequential run for reasons that have nothing
     * to do with Arquillian.
     */
    private static Map<String, String> parallelClasses(boolean enabled) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(MANAGER_SCOPE_PROPERTY, "class");
        parameters.put("junit.jupiter.execution.parallel.enabled", Boolean.toString(enabled));
        parameters.put("junit.jupiter.execution.parallel.mode.classes.default", "concurrent");
        parameters.put("junit.jupiter.execution.parallel.config.strategy", "fixed");
        parameters.put("junit.jupiter.execution.parallel.config.fixed.parallelism", "4");
        return parameters;
    }

    @Test
    public void shouldRunClassesConcurrentlyWithAManagerEach() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();
        ParallelRendezvous.expect(2);

        // when
        TestExecutionSummary summary =
            run(recorder.supplier(), parallelClasses(true), ParallelFixtureA.class, ParallelFixtureB.class);

        // then — both tests passing means both reached the barrier, so the classes really did overlap
        Assertions.assertEquals(0, summary.getTestsFailedCount(),
            () -> "The classes never met at the barrier, so they did not run concurrently."
                + System.lineSeparator() + recorder.describe());
        Assertions.assertEquals(2, summary.getTestsSucceededCount(), recorder.describe());

        Assertions.assertEquals(2, recorder.getAdaptorCount(), recorder.describe());
        Assertions.assertEquals(2, recorder.count(BEFORE_SUITE), recorder.describe());
        Assertions.assertNotEquals(
            recorder.adaptorIdFor(BEFORE_CLASS, ParallelFixtureA.class),
            recorder.adaptorIdFor(BEFORE_CLASS, ParallelFixtureB.class),
            recorder.describe());

        List<String> threadsForA = recorder.testThreadsFor(ParallelFixtureA.class);
        List<String> threadsForB = recorder.testThreadsFor(ParallelFixtureB.class);
        Assertions.assertEquals(1, threadsForA.size(), recorder.describe());
        Assertions.assertEquals(1, threadsForB.size(), recorder.describe());
        Assertions.assertNotEquals(threadsForA.get(0), threadsForB.get(0),
            () -> "Each class should have had a thread to itself." + System.lineSeparator() + recorder.describe());
    }

    /**
     * The negative control. Without parallel execution the same fixtures must fail, which is what proves
     * {@link #shouldRunClassesConcurrentlyWithAManagerEach()} would notice if concurrency silently stopped
     * happening.
     */
    @Test
    public void shouldFailWhenClassesDoNotActuallyOverlap() throws Exception {
        // given — a short wait: no partner can ever arrive on a sequential run, so there is no race to lose
        AdaptorRecorder recorder = new AdaptorRecorder();
        ParallelRendezvous.expect(2, Duration.ofSeconds(1));

        // when
        TestExecutionSummary summary =
            run(recorder.supplier(), parallelClasses(false), ParallelFixtureA.class, ParallelFixtureB.class);

        // then
        Assertions.assertEquals(0, summary.getTestsSucceededCount(),
            () -> "A sequential run must not satisfy the barrier." + System.lineSeparator() + recorder.describe());
        Assertions.assertEquals(2, summary.getTestsFailedCount(), recorder.describe());
    }
}
