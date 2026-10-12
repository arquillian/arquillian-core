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

import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.AFTER_SUITE;
import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.BEFORE_SUITE;
import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.SHUTDOWN;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.arquillian.junit5.container.fixtures.ManagerScopeFixtureA;
import org.jboss.arquillian.junit5.container.fixtures.ManagerScopeFixtureB;
import org.jboss.arquillian.junit5.container.fixtures.ManagerScopeFixtureC;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * What the rest of a run looks like after Arquillian fails to boot.
 *
 * <p>The contract: the class that triggered the failure sees the real exception, with the real type and the real
 * stack trace. Every class after it sees a wrapper explaining that initialization was already attempted and failed.
 * Initialization itself is attempted exactly once.</p>
 */
public class JUnitJupiterManagerInitializationFailureTestCase extends JUnitTestBaseClass {

    /**
     * Pins the execution order of the fixtures, which are named so that alphabetical order is the order these
     * tests talk about. Without it the run would inherit whatever class order the build happens to configure,
     * and "the class that triggered initialization" would no longer be the first one in the report.
     */
    private static Map<String, String> classesInNameOrder() {
        return Collections.singletonMap("junit.jupiter.testclass.order.default",
            "org.junit.jupiter.api.ClassOrderer$ClassName");
    }

    @Test
    public void shouldReportTheOriginalFailureToTheFirstClassAndAWrapperToTheRest() throws Exception {
        // given
        IllegalStateException bootFailure = new IllegalStateException("container boot failed");
        AdaptorRecorder recorder = new AdaptorRecorder().failBeforeSuiteWith(() -> bootFailure);

        // when
        TestExecutionSummary summary = run(recorder.supplier(), classesInNameOrder(),
            ManagerScopeFixtureA.class, ManagerScopeFixtureB.class, ManagerScopeFixtureC.class);

        // then
        Assertions.assertEquals(0, summary.getTestsSucceededCount());
        Assertions.assertEquals(1, recorder.count(BEFORE_SUITE),
            () -> "A failed initialization must not be retried." + System.lineSeparator() + recorder.describe());

        // Sequential execution, so the summary lists failures in the order the classes finished, and the class
        // that triggered initialization - the first one to run - is the first one in the list.
        List<TestExecutionSummary.Failure> failures = summary.getFailures();
        Assertions.assertEquals(3, failures.size(), () -> "Expected one failure per class, got: " + describe(failures));

        Assertions.assertSame(bootFailure, failures.get(0).getException(),
            () -> "The class that triggered initialization must be the one to see the unwrapped original failure, "
                + "got: " + describe(failures));

        for (TestExecutionSummary.Failure later : failures.subList(1, failures.size())) {
            Throwable wrapper = later.getException();
            Assertions.assertNotSame(bootFailure, wrapper,
                () -> "Only the triggering class may see the original failure, but " + nameOf(later)
                    + " saw it too: " + describe(failures));
            Assertions.assertTrue(
                wrapper.getMessage() != null
                    && wrapper.getMessage().contains("Arquillian initialization has already been attempted"),
                () -> "Later classes should get the already-attempted wrapper, got: " + describe(failures));
            Assertions.assertSame(bootFailure, wrapper.getCause(),
                () -> "The wrapper must carry the original failure as its cause, got: " + wrapper.getCause());
        }
    }

    /**
     * An {@code Error} out of the boot - a {@code NoClassDefFoundError} from a half-assembled classpath is the
     * usual one - must be cached exactly like an exception. Letting it escape the cache would make every later
     * class boot Arquillian again, and would leave the adaptor built before the failure running.
     */
    @Test
    public void shouldCacheAnErrorDuringInitializationLikeAnyOtherFailure() throws Exception {
        // given
        NoClassDefFoundError bootFailure = new NoClassDefFoundError("org/example/Missing");
        AdaptorRecorder recorder = new AdaptorRecorder().failBeforeSuiteWith(() -> bootFailure);

        // when
        TestExecutionSummary summary = run(recorder.supplier(), classesInNameOrder(),
            ManagerScopeFixtureA.class, ManagerScopeFixtureB.class, ManagerScopeFixtureC.class);

        // then
        Assertions.assertEquals(0, summary.getTestsSucceededCount());
        Assertions.assertEquals(1, recorder.count(BEFORE_SUITE),
            () -> "A failed initialization must not be retried, whatever it threw." + System.lineSeparator()
                + recorder.describe());
        Assertions.assertEquals(1, recorder.count(SHUTDOWN),
            () -> "The adaptor built before the failure must still be shut down." + System.lineSeparator()
                + recorder.describe());

        List<TestExecutionSummary.Failure> failures = summary.getFailures();
        Assertions.assertEquals(3, failures.size(), () -> "Expected one failure per class, got: " + describe(failures));
        Assertions.assertSame(bootFailure, failures.get(0).getException(),
            () -> "The class that triggered initialization must see the original error, got: " + describe(failures));
        for (TestExecutionSummary.Failure later : failures.subList(1, failures.size())) {
            Assertions.assertSame(bootFailure, later.getException().getCause(),
                () -> "Later classes must be given a wrapper carrying the original error, got: " + describe(failures));
        }
    }

    /**
     * The class that triggered the failure must be told once. Asking for the manager again from {@code afterAll}
     * would hand that class a wrapper whose cause is the very failure JUnit suppresses it onto, and the report
     * would then point back at itself with a {@code [CIRCULAR REFERENCE]}.
     */
    @Test
    public void shouldNotAddASelfReferentialWrapperToTheTriggeringClass() throws Exception {
        // given
        IllegalStateException bootFailure = new IllegalStateException("container boot failed");
        AdaptorRecorder recorder = new AdaptorRecorder().failBeforeSuiteWith(() -> bootFailure);

        // when
        TestExecutionSummary summary = run(recorder.supplier(), classesInNameOrder(),
            ManagerScopeFixtureA.class, ManagerScopeFixtureB.class);

        // then
        Throwable triggering = summary.getFailures().get(0).getException();
        Assertions.assertSame(bootFailure, triggering, () -> describe(summary.getFailures()));
        Assertions.assertEquals(0, triggering.getSuppressed().length,
            () -> "The original failure should be reported on its own, but it carries: "
                + Arrays.toString(triggering.getSuppressed()));
    }

    /**
     * {@code BeforeSuite} never completed, so {@code AfterSuite} has no business running: undeploy and stop
     * logic would go against containers that never started. The Manager built before the failure still has to be
     * released.
     */
    @Test
    public void shouldShutDownWithoutFiringAfterSuiteWhenTheBootFails() throws Exception {
        // given
        IllegalStateException bootFailure = new IllegalStateException("container boot failed");
        AdaptorRecorder recorder = new AdaptorRecorder().failBeforeSuiteWith(() -> bootFailure);

        // when
        run(recorder.supplier(), classesInNameOrder(), ManagerScopeFixtureA.class, ManagerScopeFixtureB.class);

        // then
        Assertions.assertEquals(1, recorder.count(SHUTDOWN),
            () -> "The Manager built before the failure must be released." + System.lineSeparator()
                + recorder.describe());
        Assertions.assertEquals(0, recorder.count(AFTER_SUITE),
            () -> "AfterSuite must not fire for a suite that never started." + System.lineSeparator()
                + recorder.describe());
    }

    /**
     * Each later class must get its own wrapper. Rethrowing one shared instance gives every class the same stack
     * trace, and lets suppressed exceptions accumulate on it as the run goes on.
     */
    @Test
    public void shouldGiveEachLaterClassItsOwnFailureInstance() throws Exception {
        // given
        IllegalStateException bootFailure = new IllegalStateException("container boot failed");
        AdaptorRecorder recorder = new AdaptorRecorder().failBeforeSuiteWith(() -> bootFailure);

        // when
        TestExecutionSummary summary = run(recorder.supplier(), classesInNameOrder(),
            ManagerScopeFixtureA.class, ManagerScopeFixtureB.class, ManagerScopeFixtureC.class);

        // then
        List<Throwable> failures = summary.getFailures().stream()
            .map(TestExecutionSummary.Failure::getException)
            .collect(Collectors.toList());
        Set<Throwable> distinctInstances = Collections.newSetFromMap(new IdentityHashMap<>());
        distinctInstances.addAll(failures);
        Assertions.assertEquals(failures.size(), distinctInstances.size(),
            () -> "Every class must be given its own exception instance, got: " + failures);

        List<Throwable> wrappers = failures.stream()
            .filter(t -> t != bootFailure)
            .collect(Collectors.toList());
        Assertions.assertEquals(2, wrappers.size(), () -> "Expected two later classes, got: " + wrappers);
        Assertions.assertNotSame(wrappers.get(0), wrappers.get(1),
            "Later classes must not share one exception instance");
    }

    private static String describe(List<TestExecutionSummary.Failure> failures) {
        return failures.stream()
            .map(failure -> nameOf(failure) + " -> " + failure.getException())
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String nameOf(TestExecutionSummary.Failure failure) {
        return failure.getTestIdentifier().getDisplayName();
    }
}
