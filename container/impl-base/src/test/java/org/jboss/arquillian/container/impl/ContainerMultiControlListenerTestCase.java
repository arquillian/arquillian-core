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
package org.jboss.arquillian.container.impl;

import java.util.List;
import org.jboss.arquillian.container.spi.ContainerMultiControlListener;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartClassContainers;
import org.jboss.arquillian.container.spi.event.StartSuiteContainers;
import org.jboss.arquillian.container.spi.event.StopClassContainers;
import org.jboss.arquillian.container.spi.event.StopManualContainers;
import org.jboss.arquillian.container.spi.event.StopSuiteContainers;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies that {@link ContainerMultiControlListener} instances registered via
 * {@link org.jboss.arquillian.core.spi.Manager#addListener(Class, Object)} are notified
 * for multi-container control events.
 */
public class ContainerMultiControlListenerTestCase extends AbstractContainerTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ContainerMultiControlListenerAdaptor.class);
    }

    @Test
    public void shouldNotifyListenerOnSetupContainers() throws Exception {
        TrackingContainerMultiControlListener listener = new TrackingContainerMultiControlListener();
        getManager().addListener(ContainerMultiControlListener.class, listener);

        fire(new SetupContainers());
        Assert.assertTrue("setupContainers() should have been called", listener.setupContainers);
    }

    @Test
    public void shouldNotifyListenerOnStartSuiteContainers() throws Exception {
        TrackingContainerMultiControlListener listener = new TrackingContainerMultiControlListener();
        getManager().addListener(ContainerMultiControlListener.class, listener);

        fire(new StartSuiteContainers());
        Assert.assertTrue("startSuiteContainers() should have been called", listener.startSuiteContainers);
    }

    @Test
    public void shouldNotifyListenerOnStartClassContainers() throws Exception {
        TrackingContainerMultiControlListener listener = new TrackingContainerMultiControlListener();
        getManager().addListener(ContainerMultiControlListener.class, listener);

        fire(new StartClassContainers());
        Assert.assertTrue("startClassContainers() should have been called", listener.startClassContainers);
    }

    @Test
    public void shouldNotifyListenerOnStopSuiteContainers() throws Exception {
        TrackingContainerMultiControlListener listener = new TrackingContainerMultiControlListener();
        getManager().addListener(ContainerMultiControlListener.class, listener);

        fire(new StopSuiteContainers());
        Assert.assertTrue("stopSuiteContainers() should have been called", listener.stopSuiteContainers);
    }

    @Test
    public void shouldNotifyListenerOnStopClassContainers() throws Exception {
        TrackingContainerMultiControlListener listener = new TrackingContainerMultiControlListener();
        getManager().addListener(ContainerMultiControlListener.class, listener);

        fire(new StopClassContainers());
        Assert.assertTrue("stopClassContainers() should have been called", listener.stopClassContainers);
    }

    @Test
    public void shouldNotifyListenerOnStopManualContainers() throws Exception {
        TrackingContainerMultiControlListener listener = new TrackingContainerMultiControlListener();
        getManager().addListener(ContainerMultiControlListener.class, listener);

        fire(new StopManualContainers());
        Assert.assertTrue("stopManualContainers() should have been called", listener.stopManualContainers);
    }

    @Test
    public void shouldNotifyListenerOnDeployManagedDeployments() throws Exception {
        TrackingContainerMultiControlListener listener = new TrackingContainerMultiControlListener();
        getManager().addListener(ContainerMultiControlListener.class, listener);

        fire(new DeployManagedDeployments());
        Assert.assertTrue("deployManagedDeployments() should have been called", listener.deployManagedDeployments);
    }

    @Test
    public void shouldNotifyListenerOnUndeployManagedDeployments() throws Exception {
        TrackingContainerMultiControlListener listener = new TrackingContainerMultiControlListener();
        getManager().addListener(ContainerMultiControlListener.class, listener);

        fire(new UnDeployManagedDeployments());
        Assert.assertTrue("undeployManagedDeployments() should have been called",
            listener.undeployManagedDeployments);
    }

    private static class TrackingContainerMultiControlListener implements ContainerMultiControlListener {
        boolean setupContainers = false;
        boolean startSuiteContainers = false;
        boolean startClassContainers = false;
        boolean stopSuiteContainers = false;
        boolean stopClassContainers = false;
        boolean stopManualContainers = false;
        boolean deployManagedDeployments = false;
        boolean undeployManagedDeployments = false;

        @Override
        public void setupContainers() throws Exception {
            setupContainers = true;
        }

        @Override
        public void startSuiteContainers() throws Exception {
            startSuiteContainers = true;
        }

        @Override
        public void startClassContainers() throws Exception {
            startClassContainers = true;
        }

        @Override
        public void stopSuiteContainers() throws Exception {
            stopSuiteContainers = true;
        }

        @Override
        public void stopClassContainers() throws Exception {
            stopClassContainers = true;
        }

        @Override
        public void stopManualContainers() throws Exception {
            stopManualContainers = true;
        }

        @Override
        public void deployManagedDeployments() throws Exception {
            deployManagedDeployments = true;
        }

        @Override
        public void undeployManagedDeployments() throws Exception {
            undeployManagedDeployments = true;
        }
    }
}
