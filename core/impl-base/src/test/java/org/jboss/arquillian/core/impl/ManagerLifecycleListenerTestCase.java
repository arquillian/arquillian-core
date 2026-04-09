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

import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.core.spi.ManagerLifecycleListener;
import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies that {@link ManagerLifecycleListener} instances registered via
 * {@link Manager#addListener(Class, Object)} are notified when the manager starts and stops.
 */
public class ManagerLifecycleListenerTestCase {

    @Test
    public void shouldNotifyListenerOnManagerStarted() throws Exception {
        TrackingManagerLifecycleListener listener = new TrackingManagerLifecycleListener();

        Manager manager = ManagerBuilder.from()
            .extension(ManagerLifecycleListenerAdaptor.class)
            .create();
        manager.addListener(ManagerLifecycleListener.class, listener);
        manager.start();

        Assert.assertTrue("managerStarted() should have been called", listener.started);
        Assert.assertFalse("managerStopping() should not yet have been called", listener.stopping);

        manager.shutdown();

        Assert.assertTrue("managerStopping() should have been called after shutdown", listener.stopping);
    }

    @Test
    public void shouldNotifyMultipleListeners() throws Exception {
        TrackingManagerLifecycleListener listener1 = new TrackingManagerLifecycleListener();
        TrackingManagerLifecycleListener listener2 = new TrackingManagerLifecycleListener();

        Manager manager = ManagerBuilder.from()
            .extension(ManagerLifecycleListenerAdaptor.class)
            .create();
        manager.addListener(ManagerLifecycleListener.class, listener1);
        manager.addListener(ManagerLifecycleListener.class, listener2);
        manager.start();

        Assert.assertTrue("listener1.managerStarted() should have been called", listener1.started);
        Assert.assertTrue("listener2.managerStarted() should have been called", listener2.started);

        manager.shutdown();

        Assert.assertTrue("listener1.managerStopping() should have been called", listener1.stopping);
        Assert.assertTrue("listener2.managerStopping() should have been called", listener2.stopping);
    }

    @Test
    public void shouldReturnEmptyListWhenNoListenersRegistered() throws Exception {
        Manager manager = ManagerBuilder.from().create();
        Assert.assertTrue(
            "getListeners() should return empty list when none registered",
            manager.getListeners(ManagerLifecycleListener.class).isEmpty());
        manager.shutdown();
    }

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
