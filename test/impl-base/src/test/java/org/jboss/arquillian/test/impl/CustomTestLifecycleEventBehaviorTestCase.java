/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.test.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestLifecycleListener;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Documents the behavior of the {@link TestLifecycleListener} SPI when custom
 * {@link BeforeTestLifecycleEvent} subtypes are fired — as done by JUnit 4
 * ({@code BeforeRules}, {@code AfterRules}, {@code RulesEnrichment}) and
 * JUnit 5 ({@code RunModeEvent}, {@code BeforeTestExecutionEvent}).
 *
 * <h2>Key Findings</h2>
 * <ol>
 *   <li>{@code TestLifecycleListenerAdaptor} observes specific concrete types
 *       ({@code Before}, {@code After}, etc.), NOT the abstract base class
 *       {@code BeforeTestLifecycleEvent} or {@code TestLifecycleEvent}.</li>
 *   <li>Therefore, custom events like JUnit 4's {@code BeforeRules} (which
 *       extends {@code BeforeTestLifecycleEvent}) are <em>invisible</em> to
 *       {@code TestLifecycleListener}. They only reach {@code @Observes}
 *       observers.</li>
 *   <li>This is a known gap: the listener SPI does not yet provide a hook for
 *       framework-specific custom lifecycle events. A general-purpose hook
 *       (e.g. {@code fireCustomLifecycle} equivalent in the SPI) would be
 *       needed to bridge this gap.</li>
 *   <li>The standard {@code Before} event DOES trigger {@code TestLifecycleListener},
 *       confirming the adaptor works correctly for known event types.</li>
 * </ol>
 */
public class CustomTestLifecycleEventBehaviorTestCase extends AbstractTestTestBase {

    // -------------------------------------------------------------------------
    // Simulated custom events — mirrors what JUnit 4 / JUnit 5 do externally
    // -------------------------------------------------------------------------

    /**
     * Simulates JUnit 4's {@code BeforeRules} event, which extends
     * {@code BeforeTestLifecycleEvent} with extra context (statement, TestClass).
     */
    static class SimulatedBeforeRules extends BeforeTestLifecycleEvent {
        SimulatedBeforeRules(Object testInstance, Method testMethod) {
            super(testInstance, testMethod, LifecycleMethodExecutor.NO_OP);
        }
    }

    /**
     * Simulates JUnit 5's {@code RunModeEvent}, which also extends
     * {@code BeforeTestLifecycleEvent}.
     */
    static class SimulatedRunModeEvent extends BeforeTestLifecycleEvent {
        private boolean runAsClient = true;

        SimulatedRunModeEvent(Object testInstance, Method testMethod) {
            super(testInstance, testMethod);
        }

        boolean isRunAsClient() {
            return runAsClient;
        }
    }

    // -------------------------------------------------------------------------
    // Traditional @Observes observer — must continue working
    // -------------------------------------------------------------------------

    static class CustomEventLegacyObserver {
        final List<BeforeTestLifecycleEvent> received = new ArrayList<BeforeTestLifecycleEvent>();

        /**
         * Observes the abstract supertype — receives BOTH custom events and the
         * standard {@code Before} event (since Before extends
         * BeforeTestLifecycleEvent).
         */
        public void onAnyBeforeLifecycle(@Observes BeforeTestLifecycleEvent event) {
            received.add(event);
        }
    }

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(TestLifecycleListenerAdaptor.class);
        extensions.add(CustomEventLegacyObserver.class);
    }

    /**
     * FINDING 1: The standard {@code Before} event triggers
     * {@code TestLifecycleListener.before()} — the listener SPI works for
     * known event types.
     */
    @Test
    public void standardBeforeEventTriggersTestLifecycleListener() throws Exception {
        TrackingTestLifecycleListener listener = new TrackingTestLifecycleListener();
        getManager().addListener(TestLifecycleListener.class, listener);

        Method method = getClass().getMethod(
            "standardBeforeEventTriggersTestLifecycleListener");
        fire(new Before(this, method, LifecycleMethodExecutor.NO_OP));

        Assert.assertTrue(
            "TestLifecycleListener.before() should be called for standard Before event",
            listener.beforeCalled);
    }

    /**
     * FINDING 2: {@code SimulatedBeforeRules} (extending
     * {@code BeforeTestLifecycleEvent}) does NOT trigger
     * {@code TestLifecycleListener.before()}.
     * <p>
     * The adaptor observes {@code Before} specifically, not the abstract
     * {@code BeforeTestLifecycleEvent} supertype.
     * This is the GAP: JUnit 4's BeforeRules / AfterRules, JUnit 5's RunModeEvent,
     * BeforeTestExecutionEvent etc. are invisible to the listener SPI.
     */
    @Test
    public void customBeforeRulesEventDoesNotTriggerTestLifecycleListener() throws Exception {
        TrackingTestLifecycleListener listener = new TrackingTestLifecycleListener();
        getManager().addListener(TestLifecycleListener.class, listener);

        Method method = getClass().getMethod(
            "customBeforeRulesEventDoesNotTriggerTestLifecycleListener");
        fire(new SimulatedBeforeRules(this, method));

        Assert.assertFalse(
            "TestLifecycleListener.before() must NOT be called for custom BeforeRules event "
                + "(gap: custom subtypes of BeforeTestLifecycleEvent bypass the listener SPI)",
            listener.beforeCalled);
    }

    /**
     * FINDING 3: {@code SimulatedRunModeEvent} (extending
     * {@code BeforeTestLifecycleEvent}) also does NOT trigger
     * {@code TestLifecycleListener.before()}.
     * <p>
     * Confirms the same gap applies to JUnit 5 custom events.
     */
    @Test
    public void customRunModeEventDoesNotTriggerTestLifecycleListener() throws Exception {
        TrackingTestLifecycleListener listener = new TrackingTestLifecycleListener();
        getManager().addListener(TestLifecycleListener.class, listener);

        Method method = getClass().getMethod(
            "customRunModeEventDoesNotTriggerTestLifecycleListener");
        fire(new SimulatedRunModeEvent(this, method));

        Assert.assertFalse(
            "TestLifecycleListener.before() must NOT be called for custom RunModeEvent "
                + "(gap: custom subtypes of BeforeTestLifecycleEvent bypass the listener SPI)",
            listener.beforeCalled);
    }

    /**
     * FINDING 4: Custom events STILL reach {@code @Observes} observers of the
     * correct type — the legacy mechanism is unaffected.
     * <p>
     * An observer of {@code BeforeTestLifecycleEvent} (the supertype) receives
     * both {@code SimulatedBeforeRules} and the standard {@code Before} event,
     * because {@code @Observes} uses assignability.
     */
    @Test
    public void customEventsStillReachLegacyObserversByTypeCompatibility() throws Exception {
        CustomEventLegacyObserver observer =
            getManager().getExtension(CustomEventLegacyObserver.class);
        Method method = getClass().getMethod(
            "customEventsStillReachLegacyObserversByTypeCompatibility");

        // Standard Before event — reaches observer via BeforeTestLifecycleEvent
        fire(new Before(this, method, LifecycleMethodExecutor.NO_OP));

        // Custom BeforeRules — also reaches the same observer
        fire(new SimulatedBeforeRules(this, method));

        // Custom RunModeEvent — also reaches the same observer
        fire(new SimulatedRunModeEvent(this, method));

        Assert.assertEquals(
            "Observer of BeforeTestLifecycleEvent should receive Before + BeforeRules + RunModeEvent",
            3, observer.received.size());
        Assert.assertTrue("First event should be Before",
            observer.received.get(0) instanceof Before);
        Assert.assertTrue("Second event should be SimulatedBeforeRules",
            observer.received.get(1) instanceof SimulatedBeforeRules);
        Assert.assertTrue("Third event should be SimulatedRunModeEvent",
            observer.received.get(2) instanceof SimulatedRunModeEvent);
    }

    /**
     * FINDING 5: The GAP in concrete numbers — when all three events fire,
     * the listener SPI callback count vs the legacy observer count differs.
     * <p>
     * This confirms the asymmetry: listener SPI coverage is strict (exact type),
     * while {@code @Observes} coverage is polymorphic (assignability).
     */
    @Test
    public void demonstratesGapBetweenListenerSpiAndLegacyObserver() throws Exception {
        TrackingTestLifecycleListener listener = new TrackingTestLifecycleListener();
        getManager().addListener(TestLifecycleListener.class, listener);

        CustomEventLegacyObserver observer =
            getManager().getExtension(CustomEventLegacyObserver.class);

        Method method = getClass().getMethod(
            "demonstratesGapBetweenListenerSpiAndLegacyObserver");

        fire(new Before(this, method, LifecycleMethodExecutor.NO_OP));
        fire(new SimulatedBeforeRules(this, method));
        fire(new SimulatedRunModeEvent(this, method));

        // Legacy observer receives all 3 (polymorphic via BeforeTestLifecycleEvent)
        Assert.assertEquals(
            "Legacy @Observes receives all 3 events (polymorphic assignability)",
            3, observer.received.size());

        // Listener SPI receives only 1 (the standard Before)
        Assert.assertEquals(
            "Listener SPI before() is called only once — only for the standard Before event. "
                + "Custom subtypes (BeforeRules, RunModeEvent) are invisible to the listener SPI.",
            1, listener.beforeCallCount);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static class TrackingTestLifecycleListener implements TestLifecycleListener {
        boolean beforeCalled = false;
        int beforeCallCount = 0;

        @Override
        public void before(Object testInstance, Method testMethod,
            LifecycleMethodExecutor executor) throws Exception {
            beforeCalled = true;
            beforeCallCount++;
        }
    }
}
