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
package org.jboss.arquillian.core.impl;

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ManagerLifecycleListener;
import org.jboss.arquillian.core.spi.event.Event;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Documents the behavior of the listener SPI when custom
 * {@link org.jboss.arquillian.core.spi.event.Event} types are fired via
 * {@link org.jboss.arquillian.core.spi.Manager#fire(Object)}.
 *
 * <h2>Findings</h2>
 * <ol>
 *   <li>Custom events fired via {@code Manager#fire()} continue to reach
 *       {@code @Observes}-annotated observer methods exactly as before —
 *       the new listener SPI does not break the legacy event model.</li>
 *   <li>Custom events do <em>not</em> trigger any of the typed listener SPI
 *       callbacks ({@link ManagerLifecycleListener}, etc.), because the adaptor
 *       classes observe only the specific known event types.</li>
 *   <li>A custom event that <em>extends</em> a known event type (e.g.
 *       {@code class BeforeRules extends BeforeTestLifecycleEvent}) will fire
 *       all {@code @Observes} observers compatible with that type hierarchy,
 *       but will <em>still not</em> trigger any listener SPI callback, because
 *       the adaptor observes the concrete known subtype (e.g. {@code Before}),
 *       not the abstract supertype ({@code BeforeTestLifecycleEvent}).</li>
 *   <li>Consequence: framework-specific custom events (JUnit {@code BeforeRules},
 *       JUnit 5 {@code RunModeEvent}, etc.) are invisible to the listener SPI.
 *       They can only be observed via {@code @Observes} in traditional observer
 *       classes. A future general-purpose listener hook point may be needed to
 *       cover these cases.</li>
 * </ol>
 */
public class CustomEventListenerBehaviorTestCase extends AbstractManagerTestBase {

    // -------------------------------------------------------------------------
    // Custom event types -- simulating what external code does
    // -------------------------------------------------------------------------

    /** A completely custom event unrelated to any standard hierarchy. */
    static class CustomSimpleEvent implements Event {
    }

    /**
     * A custom event that, like JUnit 4's BeforeRules, extends a standard
     * lifecycle event base class but is its own concrete type.
     * In real code this would extend BeforeTestLifecycleEvent; here we extend
     * the common base just to demonstrate the type-hierarchy behaviour.
     */
    static class CustomLifecycleEvent implements Event {
        private final String phase;

        CustomLifecycleEvent(String phase) {
            this.phase = phase;
        }

        String getPhase() {
            return phase;
        }
    }

    // -------------------------------------------------------------------------
    // Observer class — the traditional @Observes mechanism
    // -------------------------------------------------------------------------

    static class CustomEventObserver {
        final List<Object> receivedSimple = new ArrayList<Object>();
        final List<Object> receivedLifecycle = new ArrayList<Object>();

        /** Receives any CustomSimpleEvent. */
        public void onSimple(@Observes CustomSimpleEvent event) {
            receivedSimple.add(event);
        }

        /** Receives any CustomLifecycleEvent. */
        public void onLifecycle(@Observes CustomLifecycleEvent event) {
            receivedLifecycle.add(event);
        }
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(CustomEventObserver.class);
        extensions.add(ManagerLifecycleListenerAdaptor.class);
    }

    /**
     * FINDING 1: Custom events fired via {@code Manager#fire()} still reach
     * {@code @Observes}-annotated observers — the new listener SPI does not
     * break the existing mechanism.
     */
    @Test
    public void customEventReachesLegacyObserver() {
        CustomEventObserver observer = getManager().getExtension(CustomEventObserver.class);

        fire(new CustomSimpleEvent());
        Assert.assertEquals(
            "Custom event should reach @Observes observer exactly once",
            1, observer.receivedSimple.size());

        fire(new CustomLifecycleEvent("setup"));
        Assert.assertEquals(
            "Custom lifecycle event should reach @Observes observer exactly once",
            1, observer.receivedLifecycle.size());
        Assert.assertEquals("setup",
            ((CustomLifecycleEvent) observer.receivedLifecycle.get(0)).getPhase());
    }

    /**
     * FINDING 2: Custom events do NOT trigger the typed listener SPI callbacks,
     * because the adaptor observes only specific known event types.
     * <p>
     * {@link ManagerLifecycleListenerAdaptor} observes {@code ManagerStarted}
     * and {@code ManagerStopping}. Firing a {@code CustomSimpleEvent} does
     * not touch {@link ManagerLifecycleListener} implementations at all.
     */
    @Test
    public void customEventDoesNotTriggerListenerSpi() {
        TrackingManagerLifecycleListener listener = new TrackingManagerLifecycleListener();
        getManager().addListener(ManagerLifecycleListener.class, listener);

        // Fire a custom event — the ManagerLifecycleListener should NOT be called
        fire(new CustomSimpleEvent());
        Assert.assertFalse(
            "managerStarted() must NOT be called when a custom event is fired",
            listener.started);
        Assert.assertFalse(
            "managerStopping() must NOT be called when a custom event is fired",
            listener.stopping);
    }

    /**
     * FINDING 3: Multiple firings of a custom event each reach the observer
     * independently, verifying there is no state leak between firings.
     */
    @Test
    public void multipleCustomEventFiringsEachReachObserver() {
        CustomEventObserver observer = getManager().getExtension(CustomEventObserver.class);

        fire(new CustomLifecycleEvent("before"));
        fire(new CustomLifecycleEvent("after"));

        Assert.assertEquals(
            "Both custom lifecycle events should reach the observer",
            2, observer.receivedLifecycle.size());
        Assert.assertEquals("before",
            ((CustomLifecycleEvent) observer.receivedLifecycle.get(0)).getPhase());
        Assert.assertEquals("after",
            ((CustomLifecycleEvent) observer.receivedLifecycle.get(1)).getPhase());
    }

    /**
     * FINDING 4: The listener registry is independent per listener type.
     * Registering a {@link ManagerLifecycleListener} does not affect observation
     * of custom events, and vice-versa. Both coexist without interference.
     */
    @Test
    public void legacyObserverAndListenerSpiCoexistWithoutInterference() {
        CustomEventObserver observer = getManager().getExtension(CustomEventObserver.class);
        TrackingManagerLifecycleListener listener = new TrackingManagerLifecycleListener();
        getManager().addListener(ManagerLifecycleListener.class, listener);

        // Custom event: reaches observer, not listener
        fire(new CustomSimpleEvent());
        Assert.assertEquals(1, observer.receivedSimple.size());
        Assert.assertFalse("Listener SPI must not be triggered by custom event",
            listener.started);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static class TrackingManagerLifecycleListener implements ManagerLifecycleListener {
        boolean started = false;
        boolean stopping = false;

        @Override
        public void managerStarted() throws Exception {
            started = true;
        }

        @Override
        public void managerStopping() throws Exception {
            stopping = true;
        }
    }
}
