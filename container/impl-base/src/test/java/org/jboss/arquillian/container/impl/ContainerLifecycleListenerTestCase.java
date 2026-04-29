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
import org.jboss.arquillian.container.spi.ContainerLifecycleListener;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterKill;
import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeKill;
import org.jboss.arquillian.container.spi.event.container.BeforeSetup;
import org.jboss.arquillian.container.spi.event.container.BeforeStart;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Verifies that {@link ContainerLifecycleListener} instances registered via
 * {@link org.jboss.arquillian.core.spi.Manager#addListener(Class, Object)} are notified
 * for container notification events.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class ContainerLifecycleListenerTestCase extends AbstractContainerTestBase {

    @Mock
    private DeployableContainer deployableContainer;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ContainerLifecycleListenerAdaptor.class);
    }

    @Test
    public void shouldNotifyListenerOnSetupEvents() throws Exception {
        TrackingContainerLifecycleListener listener = new TrackingContainerLifecycleListener();
        getManager().addListener(ContainerLifecycleListener.class, listener);

        fire(new BeforeSetup(deployableContainer));
        Assert.assertTrue("beforeSetup() should have been called", listener.beforeSetup);
        Assert.assertSame(deployableContainer, listener.deployableContainer);

        fire(new AfterSetup(deployableContainer));
        Assert.assertTrue("afterSetup() should have been called", listener.afterSetup);
    }

    @Test
    public void shouldNotifyListenerOnStartEvents() throws Exception {
        TrackingContainerLifecycleListener listener = new TrackingContainerLifecycleListener();
        getManager().addListener(ContainerLifecycleListener.class, listener);

        fire(new BeforeStart(deployableContainer));
        Assert.assertTrue("beforeStart() should have been called", listener.beforeStart);

        fire(new AfterStart(deployableContainer));
        Assert.assertTrue("afterStart() should have been called", listener.afterStart);
    }

    @Test
    public void shouldNotifyListenerOnStopEvents() throws Exception {
        TrackingContainerLifecycleListener listener = new TrackingContainerLifecycleListener();
        getManager().addListener(ContainerLifecycleListener.class, listener);

        fire(new BeforeStop(deployableContainer));
        Assert.assertTrue("beforeStop() should have been called", listener.beforeStop);

        fire(new AfterStop(deployableContainer));
        Assert.assertTrue("afterStop() should have been called", listener.afterStop);
    }

    @Test
    public void shouldNotifyListenerOnKillEvents() throws Exception {
        TrackingContainerLifecycleListener listener = new TrackingContainerLifecycleListener();
        getManager().addListener(ContainerLifecycleListener.class, listener);

        fire(new BeforeKill(deployableContainer));
        Assert.assertTrue("beforeKill() should have been called", listener.beforeKill);

        fire(new AfterKill(deployableContainer));
        Assert.assertTrue("afterKill() should have been called", listener.afterKill);
    }

    @Test
    public void shouldNotifyListenerOnDeployEvents() throws Exception {
        TrackingContainerLifecycleListener listener = new TrackingContainerLifecycleListener();
        getManager().addListener(ContainerLifecycleListener.class, listener);

        DeploymentDescription description =
            new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class));

        fire(new BeforeDeploy(deployableContainer, description));
        Assert.assertTrue("beforeDeploy() should have been called", listener.beforeDeploy);
        Assert.assertSame(description, listener.deploymentDescription);

        fire(new AfterDeploy(deployableContainer, description));
        Assert.assertTrue("afterDeploy() should have been called", listener.afterDeploy);
    }

    @Test
    public void shouldNotifyListenerOnUndeployEvents() throws Exception {
        TrackingContainerLifecycleListener listener = new TrackingContainerLifecycleListener();
        getManager().addListener(ContainerLifecycleListener.class, listener);

        DeploymentDescription description =
            new DeploymentDescription("test", ShrinkWrap.create(JavaArchive.class));

        fire(new BeforeUnDeploy(deployableContainer, description));
        Assert.assertTrue("beforeUndeploy() should have been called", listener.beforeUndeploy);

        fire(new AfterUnDeploy(deployableContainer, description));
        Assert.assertTrue("afterUndeploy() should have been called", listener.afterUndeploy);
    }

    private static class TrackingContainerLifecycleListener implements ContainerLifecycleListener {
        boolean beforeSetup = false;
        boolean afterSetup = false;
        boolean beforeStart = false;
        boolean afterStart = false;
        boolean beforeStop = false;
        boolean afterStop = false;
        boolean beforeKill = false;
        boolean afterKill = false;
        boolean beforeDeploy = false;
        boolean afterDeploy = false;
        boolean beforeUndeploy = false;
        boolean afterUndeploy = false;
        DeployableContainer<?> deployableContainer = null;
        DeploymentDescription deploymentDescription = null;

        @Override
        public void beforeSetup(DeployableContainer<?> dc) throws Exception {
            beforeSetup = true;
            deployableContainer = dc;
        }

        @Override
        public void afterSetup(DeployableContainer<?> dc) throws Exception {
            afterSetup = true;
        }

        @Override
        public void beforeStart(DeployableContainer<?> dc) throws Exception {
            beforeStart = true;
        }

        @Override
        public void afterStart(DeployableContainer<?> dc) throws Exception {
            afterStart = true;
        }

        @Override
        public void beforeStop(DeployableContainer<?> dc) throws Exception {
            beforeStop = true;
        }

        @Override
        public void afterStop(DeployableContainer<?> dc) throws Exception {
            afterStop = true;
        }

        @Override
        public void beforeKill(DeployableContainer<?> dc) throws Exception {
            beforeKill = true;
        }

        @Override
        public void afterKill(DeployableContainer<?> dc) throws Exception {
            afterKill = true;
        }

        @Override
        public void beforeDeploy(DeployableContainer<?> dc, DeploymentDescription description) throws Exception {
            beforeDeploy = true;
            deploymentDescription = description;
        }

        @Override
        public void afterDeploy(DeployableContainer<?> dc, DeploymentDescription description) throws Exception {
            afterDeploy = true;
        }

        @Override
        public void beforeUndeploy(DeployableContainer<?> dc, DeploymentDescription description) throws Exception {
            beforeUndeploy = true;
        }

        @Override
        public void afterUndeploy(DeployableContainer<?> dc, DeploymentDescription description) throws Exception {
            afterUndeploy = true;
        }
    }
}
