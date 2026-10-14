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
import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.AFTER_SUITE;
import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.BEFORE_CLASS;
import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.BEFORE_SUITE;
import static org.jboss.arquillian.junit5.container.AdaptorRecorder.Event.SHUTDOWN;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.arquillian.junit5.container.fixtures.InheritedNestedFixture;
import org.jboss.arquillian.junit5.container.fixtures.InstanceRegisteredFixture;
import org.jboss.arquillian.junit5.container.fixtures.ManagerScopeFixtureA;
import org.jboss.arquillian.junit5.container.fixtures.ManagerScopeFixtureB;
import org.jboss.arquillian.junit5.container.fixtures.ManagerScopeNestedFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Covers {@code arquillian.junit.jupiter.manager.scope}: how many Arquillian managers a run creates, which test
 * classes each one serves, and how long each one lives.
 */
public class JUnitJupiterManagerScopeTestCase extends JUnitTestBaseClass {

    private static Map<String, String> scope(String value) {
        return Collections.singletonMap(MANAGER_SCOPE_PROPERTY, value);
    }

    /**
     * Pins the execution order of the fixtures, which are named so that alphabetical order is the order the
     * assertions expect. Without it a build that configures its own class orderer would reorder them.
     */
    private static Map<String, String> classesInNameOrder() {
        return Collections.singletonMap("junit.jupiter.testclass.order.default",
            "org.junit.jupiter.api.ClassOrderer$ClassName");
    }

    /**
     * The classic Arquillian model, and what every container that boots once and serves the whole run depends on.
     * Guards it against regressions from the class-scope work.
     */
    @Test
    public void shouldShareASingleManagerAcrossClassesByDefault() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when — no scope configured at all
        TestExecutionSummary summary = run(recorder.supplier(), classesInNameOrder(),
            ManagerScopeFixtureA.class, ManagerScopeFixtureB.class);

        // then
        Assertions.assertEquals(2, summary.getTestsSucceededCount(), recorder.describe());
        Assertions.assertEquals(1, recorder.getAdaptorCount(), recorder.describe());
        Assertions.assertEquals(1, recorder.count(BEFORE_SUITE), recorder.describe());
        Assertions.assertEquals(1, recorder.count(AFTER_SUITE), recorder.describe());
        Assertions.assertEquals(List.of(ManagerScopeFixtureA.class, ManagerScopeFixtureB.class),
            recorder.testClassesFor(BEFORE_CLASS), recorder.describe());
    }

    /**
     * Configuring the default explicitly must behave exactly like leaving it out.
     */
    @Test
    public void shouldShareASingleManagerAcrossClassesInSuiteScope() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when
        TestExecutionSummary summary =
            run(recorder.supplier(), scope("suite"), ManagerScopeFixtureA.class, ManagerScopeFixtureB.class);

        // then
        Assertions.assertEquals(2, summary.getTestsSucceededCount(), recorder.describe());
        Assertions.assertEquals(1, recorder.getAdaptorCount(), recorder.describe());
        Assertions.assertEquals(1, recorder.count(BEFORE_SUITE), recorder.describe());
    }

    @Test
    public void shouldCreateOneManagerPerTestClassInClassScope() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when
        TestExecutionSummary summary =
            run(recorder.supplier(), scope("class"), ManagerScopeFixtureA.class, ManagerScopeFixtureB.class);

        // then
        Assertions.assertEquals(2, summary.getTestsSucceededCount(), recorder.describe());
        Assertions.assertEquals(2, recorder.getAdaptorCount(),
            () -> "Each test class must get its own manager." + System.lineSeparator() + recorder.describe());

        int managerForA = recorder.adaptorIdFor(BEFORE_CLASS, ManagerScopeFixtureA.class);
        int managerForB = recorder.adaptorIdFor(BEFORE_CLASS, ManagerScopeFixtureB.class);
        Assertions.assertNotEquals(managerForA, managerForB, recorder.describe());

        // BeforeSuite fires once per class, not once per run
        Assertions.assertEquals(2, recorder.count(BEFORE_SUITE), recorder.describe());
        Assertions.assertEquals(1, recorder.count(managerForA, BEFORE_SUITE), recorder.describe());
        Assertions.assertEquals(1, recorder.count(managerForB, BEFORE_SUITE), recorder.describe());
    }

    /**
     * A class-scoped manager - and the container it owns - must be released when its test class finishes, not held
     * until the end of the whole run.
     */
    @Test
    public void shouldCloseEachClassScopedManagerWhenItsClassFinishes() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when — sequential, so "closed before the next one starts" is a well-defined claim
        run(recorder.supplier(), scope("class"), ManagerScopeFixtureA.class, ManagerScopeFixtureB.class);

        // then
        List<Integer> startOrder = recorder.adaptorIdsInStartOrder();
        Assertions.assertEquals(2, startOrder.size(), recorder.describe());

        int first = startOrder.get(0);
        int second = startOrder.get(1);
        Assertions.assertTrue(
            recorder.sequenceOf(first, SHUTDOWN) < recorder.sequenceOf(second, BEFORE_SUITE),
            () -> "The first manager must be shut down before the second one starts, otherwise every manager and "
                + "its container stays alive until the end of the run." + System.lineSeparator()
                + recorder.describe());
        Assertions.assertTrue(
            recorder.sequenceOf(first, AFTER_SUITE) < recorder.sequenceOf(first, SHUTDOWN),
            recorder.describe());

        // The last class is the one most easily forgotten: nothing starts after it to prove it was released.
        Assertions.assertEquals(1, recorder.count(second, AFTER_SUITE),
            () -> "The last manager must be closed too, not left running once the run is over."
                + System.lineSeparator() + recorder.describe());
        Assertions.assertEquals(1, recorder.count(second, SHUTDOWN),
            () -> "The last manager must be shut down too, otherwise its container outlives the run."
                + System.lineSeparator() + recorder.describe());
        Assertions.assertTrue(
            recorder.sequenceOf(second, AFTER_SUITE) < recorder.sequenceOf(second, SHUTDOWN),
            recorder.describe());
    }

    /**
     * A {@code @Nested} class shares its enclosing class's deployment, so it must share that class's manager too.
     * Giving it one of its own would hand its tests a manager that never fired {@code BeforeClass} and has no
     * deployment, and would fire {@code BeforeSuite} all over again.
     */
    @Test
    public void shouldShareTheEnclosingClassManagerWithNestedClassesInClassScope() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when
        TestExecutionSummary summary = run(recorder.supplier(), scope("class"), ManagerScopeNestedFixture.class);

        // then
        Assertions.assertEquals(3, summary.getTestsSucceededCount(), recorder.describe());
        Assertions.assertEquals(1, recorder.getAdaptorCount(),
            () -> "Nested classes must reuse the enclosing class's manager." + System.lineSeparator()
                + recorder.describe());
        Assertions.assertEquals(1, recorder.count(BEFORE_SUITE), recorder.describe());

        // BeforeClass fires for the outermost class only
        Assertions.assertEquals(1, recorder.count(BEFORE_CLASS), recorder.describe());
        Assertions.assertEquals(List.of(ManagerScopeNestedFixture.class),
            recorder.testClassesFor(BEFORE_CLASS), recorder.describe());
    }

    /**
     * A {@code @Nested} class declared in a superclass runs under the subclass, and its enclosing class never
     * gets a context of its own. Looking for that enclosing class in the context chain finds nothing, so the
     * manager has to be resolved from the contexts that exist rather than from the class nesting.
     */
    @Test
    public void shouldShareTheManagerWithANestedClassInheritedFromASuperclass() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when
        TestExecutionSummary summary = run(recorder.supplier(), scope("class"), InheritedNestedFixture.class);

        // then
        Assertions.assertEquals(0, summary.getTestsFailedCount(),
            () -> "An inherited @Nested class must find its enclosing class's manager."
                + System.lineSeparator() + failureMessageOf(summary) + recorder.describe());
        Assertions.assertEquals(2, summary.getTestsSucceededCount(), recorder.describe());
        Assertions.assertEquals(1, recorder.getAdaptorCount(), recorder.describe());
        Assertions.assertEquals(1, recorder.count(BEFORE_SUITE), recorder.describe());
    }

    /**
     * Registered on the instance, the extension never sees {@code beforeAll}, so the first manager lookup of the
     * run comes from a method context. Class scope still means one manager for the class, not one per test.
     */
    @Test
    public void shouldCreateOneManagerPerClassWhenTheExtensionIsRegisteredPerInstance() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when
        TestExecutionSummary summary = run(recorder.supplier(), scope("class"), InstanceRegisteredFixture.class);

        // then
        Assertions.assertEquals(3, summary.getTestsSucceededCount(), recorder.describe());
        Assertions.assertEquals(1, recorder.getAdaptorCount(),
            () -> "A manager per test method, rather than per class, would start and stop a container for every "
                + "test." + System.lineSeparator() + recorder.describe());
        Assertions.assertEquals(1, recorder.count(BEFORE_SUITE), recorder.describe());
        Assertions.assertEquals(1, recorder.count(AFTER_SUITE), recorder.describe());
    }

    /**
     * A failing {@code afterSuite()} must not cost us the report of what went wrong, nor leave the manager - and
     * the container behind it - running.
     */
    @Test
    public void shouldShutDownAndReportBothFailuresWhenTeardownFails() throws Exception {
        // given
        IllegalStateException afterSuiteFailure = new IllegalStateException("undeploy failed");
        IllegalStateException shutdownFailure = new IllegalStateException("shutdown failed");
        AdaptorRecorder recorder = new AdaptorRecorder()
            .failAfterSuiteWith(() -> afterSuiteFailure)
            .failShutdownWith(() -> shutdownFailure);

        // when
        TestExecutionSummary summary = run(recorder.supplier(), scope("class"), ManagerScopeFixtureA.class);

        // then
        Assertions.assertEquals(1, recorder.count(SHUTDOWN),
            () -> "The manager must be shut down even when afterSuite() throws." + System.lineSeparator()
                + recorder.describe());

        String report = failureMessageOf(summary);
        Assertions.assertTrue(report.contains("undeploy failed"),
            () -> "The afterSuite failure must be the one reported, but the report was: " + report);
        Assertions.assertTrue(report.contains("shutdown failed"),
            () -> "The shutdown failure must survive as a suppressed exception, but the report was: " + report);
    }

    /**
     * A misspelled scope must say so. Quietly falling back to the default would let a run that was meant to be
     * class-scoped execute suite-scoped, which fails later in ways that point nowhere near the typo.
     */
    @Test
    public void shouldRejectAnUnknownManagerScope() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when
        TestExecutionSummary summary = run(recorder.supplier(), scope("per-class"), ManagerScopeFixtureA.class);

        // then
        Assertions.assertEquals(0, summary.getTestsSucceededCount(), recorder.describe());
        Assertions.assertEquals(0, recorder.getAdaptorCount(),
            () -> "No manager should be built for an unusable configuration." + System.lineSeparator()
                + recorder.describe());

        String message = failureMessageOf(summary);
        Assertions.assertTrue(message.contains(MANAGER_SCOPE_PROPERTY),
            () -> "Failure should name the offending parameter, but was: " + message);
        Assertions.assertTrue(message.contains("per-class"),
            () -> "Failure should quote the rejected value, but was: " + message);
        Assertions.assertTrue(message.contains("suite") && message.contains("class"),
            () -> "Failure should list the accepted values, but was: " + message);
    }

    /**
     * A blank value is a configured value, not an absent one. Treating it as "use the default" would let a run
     * that was meant to be class-scoped share one manager without saying so.
     */
    @Test
    public void shouldRejectABlankManagerScope() throws Exception {
        // given
        AdaptorRecorder recorder = new AdaptorRecorder();

        // when
        TestExecutionSummary summary = run(recorder.supplier(), scope("   "), ManagerScopeFixtureA.class);

        // then
        Assertions.assertEquals(0, summary.getTestsSucceededCount(), recorder.describe());
        Assertions.assertEquals(0, recorder.getAdaptorCount(),
            () -> "No manager should be built for an unusable configuration." + System.lineSeparator()
                + recorder.describe());

        String message = failureMessageOf(summary);
        Assertions.assertTrue(message.contains(MANAGER_SCOPE_PROPERTY),
            () -> "Failure should name the offending parameter, but was: " + message);
    }

    private static String failureMessageOf(TestExecutionSummary summary) {
        return summary.getFailures().stream()
            .map(TestExecutionSummary.Failure::getException)
            .map(JUnitJupiterManagerScopeTestCase::describeThrowable)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Causes and suppressed exceptions alike, since a teardown failure can arrive either way. Visited instances
     * are tracked because a wrapper can appear as both the cause and the suppressed exception of the same
     * throwable.
     */
    private static String describeThrowable(Throwable throwable) {
        StringBuilder description = new StringBuilder();
        appendThrowable(throwable, description, Collections.newSetFromMap(new IdentityHashMap<>()));
        return description.toString();
    }

    private static void appendThrowable(Throwable throwable, StringBuilder description, Set<Throwable> visited) {
        if (throwable == null || !visited.add(throwable)) {
            return;
        }
        description.append(throwable).append(System.lineSeparator());
        appendThrowable(throwable.getCause(), description, visited);
        for (Throwable suppressed : throwable.getSuppressed()) {
            appendThrowable(suppressed, description, visited);
        }
    }
}
