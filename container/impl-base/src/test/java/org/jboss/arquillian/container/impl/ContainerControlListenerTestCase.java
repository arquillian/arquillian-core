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
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerControlListener;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.KillContainer;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Verifies that {@link ContainerControlListener} instances registered via
 * {@link org.jboss.arquillian.core.spi.Manager#addListener(Class, Object)} are notified
 * for single-container control events.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerControlListenerTestCase extends AbstractContainerTestBase {

    @Mock
    private Container container;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ContainerControlListenerAdaptor.class);
    }

    @Test
    public void shouldNotifyListenerOnSetupContainer() throws Exception {
        TrackingContainerControlListener listener = new TrackingContainerControlListener();
        getManager().addListener(ContainerControlListener.class, listener);

        fire(new SetupContainer(container));
        Assert.assertTrue("setupContainer() should have been called", listener.setupContainer);
        Assert.assertSame(container, listener.container);
    }

    @Test
    public void shouldNotifyListenerOnStartContainer() throws Exception {
        TrackingContainerControlListener listener = new TrackingContainerControlListener();
        getManager().addListener(ContainerControlListener.class, listener);

        fire(new StartContainer(container));
        Assert.assertTrue("startContainer() should have been called", listener.startContainer);
    }

    @Test
    public void shouldNotifyListenerOnStopContainer() throws Exception {
        TrackingContainerControlListener listener = new TrackingContainerControlListener();
        getManager().addListener(ContainerControlListener.class, listener);

        fire(new StopContainer(container));
        Assert.assertTrue("stopContainer() should have been called", listener.stopContainer);
    }

    @Test
    public void shouldNotifyListenerOnKillContainer() throws Exception {
        TrackingContainerControlListener listener = new TrackingContainerControlListener();
        getManager().addListener(ContainerControlListener.class, listener);

        fire(new KillContainer(container));
        Assert.assertTrue("killContainer() should have been called", listener.killContainer);
    }

    @Test
    public void shouldNotifyListenerOnDeployDeployment() throws Exception {
        TrackingContainerControlListener listener = new TrackingContainerControlListener();
        getManager().addListener(ContainerControlListener.class, listener);

        DeploymentDescription description =
            new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class));
        Deployment deployment = new Deployment(description);

        fire(new DeployDeployment(container, deployment));
        Assert.assertTrue("deployDeployment() should have been called", listener.deployDeployment);
        Assert.assertSame(container, listener.deployContainer);
        Assert.assertSame(deployment, listener.deployment);
    }

    @Test
    public void shouldNotifyListenerOnUndeployDeployment() throws Exception {
        TrackingContainerControlListener listener = new TrackingContainerControlListener();
        getManager().addListener(ContainerControlListener.class, listener);

        DeploymentDescription description =
            new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class));
        Deployment deployment = new Deployment(description);

        fire(new UnDeployDeployment(container, deployment));
        Assert.assertTrue("undeployDeployment() should have been called", listener.undeployDeployment);
    }

    private static class TrackingContainerControlListener implements ContainerControlListener {
        boolean setupContainer = false;
        boolean startContainer = false;
        boolean stopContainer = false;
        boolean killContainer = false;
        boolean deployDeployment = false;
        boolean undeployDeployment = false;
        Container container = null;
        Container deployContainer = null;
        Deployment deployment = null;

        @Override
        public void setupContainer(Container c) throws Exception {
            setupContainer = true;
            container = c;
        }

        @Override
        public void startContainer(Container c) throws Exception {
            startContainer = true;
        }

        @Override
        public void stopContainer(Container c) throws Exception {
            stopContainer = true;
        }

        @Override
        public void killContainer(Container c) throws Exception {
            killContainer = true;
        }

        @Override
        public void deployDeployment(Container c, Deployment d) throws Exception {
            deployDeployment = true;
            deployContainer = c;
            deployment = d;
        }

        @Override
        public void undeployDeployment(Container c, Deployment d) throws Exception {
            undeployDeployment = true;
        }
    }
}
