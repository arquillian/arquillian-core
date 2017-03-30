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
package org.jboss.arquillian.container.test.impl.client.container;

import java.util.List;
import junit.framework.Assert;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.impl.LocalContainerRegistry;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentTargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.KillContainer;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ClientContainerControllerTestCase
 *
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientContainerControllerTestCase extends AbstractContainerTestTestBase {
    private static final String MANAGED_SERVER_NAME = "suiteOrClassServer";

    private static final String MANUAL_SERVER_NAME = "manualServer";

    private static final String CUSTOM_SERVER_NAME = "customServer";

    private static final String UNKNOWN_SERVER = "unknown";

    private static final String DEPLOYMENT_NAME = "DEPLOYMENT";
    @Inject
    private Instance<Injector> injector;
    private ContainerRegistry registry;
    @Inject
    private Instance<ContainerController> controller;
    @Inject
    private Instance<DeploymentScenario> scenario;
    @Mock
    private ServiceLoader serviceLoader;
    @Mock
    @SuppressWarnings("rawtypes")
    private DeployableContainer deployableContainer;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ClientContainerControllerCreator.class);
    }

    @Before
    public void createSetup() {
        registry = new LocalContainerRegistry(injector.get());
        when(serviceLoader.onlyOne(eq(DeployableContainer.class))).thenReturn(deployableContainer);

        ContainerDef suiteContainerDef = mock(ContainerDef.class);
        when(suiteContainerDef.getContainerName()).thenReturn(MANAGED_SERVER_NAME);
        when(suiteContainerDef.getMode()).thenReturn("suite");

        ContainerDef manualContainerDef = Mockito.mock(ContainerDef.class);
        when(manualContainerDef.getContainerName()).thenReturn(MANUAL_SERVER_NAME);
        when(manualContainerDef.getMode()).thenReturn("manual");

        ContainerDef customContainerDef = Mockito.mock(ContainerDef.class);
        when(customContainerDef.getMode()).thenReturn("custom");
        when(customContainerDef.getContainerName()).thenReturn(CUSTOM_SERVER_NAME);

        registry.create(suiteContainerDef, serviceLoader);
        registry.create(manualContainerDef, serviceLoader);
        registry.create(customContainerDef, serviceLoader);

        bind(ApplicationScoped.class, ContainerRegistry.class, registry);
        bind(ApplicationScoped.class, DeploymentScenario.class, new DeploymentScenario());

        fire(new SetupContainers());
    }

    @Test
    public void shouldFireStartContainerEventOnStartManual() throws Exception {
        controller.get().start(MANUAL_SERVER_NAME);
        assertEventFired(StartContainer.class, 1);
    }

    @Test
    public void shouldFireStartContainerEventOnStartCustom() throws Exception {
        controller.get().start(CUSTOM_SERVER_NAME);
        assertEventFired(StartContainer.class, 1);
    }

    @Test
    public void shouldFireDeployDeploymentEventOnStartManualWhenManagedDeployment() throws Exception {
        setupAndExecuteManagedDeployment(MANUAL_SERVER_NAME);
        assertEventFired(DeployDeployment.class, 2);
    }

    // Custom Containers does not allow Managed Deployments (verified in DeploymentGenerator)
    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnStartCustomWhenManagedDeployment() throws Exception {
        setupAndExecuteManagedDeployment(CUSTOM_SERVER_NAME);
        //assertEventFired(DeployDeployment.class, 0);
    }

    @Test
    public void shouldNotFireDeployDeploymentEventOnStartManualWhenNotManagedDeployment() throws Exception {
        setupAndExecuteNonManagedDeployment(MANUAL_SERVER_NAME);
        assertEventFired(DeployDeployment.class, 0);
    }

    @Test
    public void shouldNotFireDeployDeploymentEventOnStartCustomWhenNotManagedDeployment() throws Exception {
        setupAndExecuteNonManagedDeployment(CUSTOM_SERVER_NAME);
        assertEventFired(DeployDeployment.class, 0);
    }

    @Test
    public void shouldFireUnDeployDeploymentEventOnStopManualWhenManagedDeployment() throws Exception {
        setupAndExecuteManagedUnDeployment(MANUAL_SERVER_NAME);
        assertEventFired(UnDeployDeployment.class, 1);
    }

    @Test
    public void shouldNotFireUnDeployDeploymentEventOnStopManualWhenNotManagedDeployment() throws Exception {
        setupAndExecuteNonManagedUnDeployment(MANUAL_SERVER_NAME);
        assertEventFired(UnDeployDeployment.class, 0);
    }

    @Test
    public void shouldNotFireUnDeployDeploymentEventOnStopCustomWhenNotManagedDeployment() throws Exception {
        setupAndExecuteNonManagedUnDeployment(CUSTOM_SERVER_NAME);
        assertEventFired(UnDeployDeployment.class, 0);
    }

    @Test
    public void shouldFireStartContainerEventOnStartWithOverrides() throws Exception {
        controller.get().start(MANUAL_SERVER_NAME, new Config().add("managementPort", "19999").map());

        assertEventFired(StartContainer.class, 1);
    }

    @Test
    public void shouldFireStopContainerEventOnStop() throws Exception {
        controller.get().stop(MANUAL_SERVER_NAME);

        assertEventFired(StopContainer.class, 1);
    }

    @Test
    public void shouldFireKillContainerEventOnKill() throws Exception {
        controller.get().kill(MANUAL_SERVER_NAME);

        assertEventFired(KillContainer.class, 1);
    }

    @Test
    public void shouldBeAbleToCheckTheStateOfAServer() throws Exception {
        Container contianer = registry.getContainer(CUSTOM_SERVER_NAME);
        contianer.setState(Container.State.STOPPED);
        Assert.assertFalse(controller.get().isStarted(CUSTOM_SERVER_NAME));

        contianer.setState(Container.State.STARTED);
        Assert.assertTrue(controller.get().isStarted(CUSTOM_SERVER_NAME));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnStartWhenManaged() throws Exception {
        controller.get().start(MANAGED_SERVER_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnStartWithOverridesWhenManaged() throws Exception {
        controller.get().start(MANAGED_SERVER_NAME, new Config().add("managementPort", "19999").map());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnStopWhenManaged() throws Exception {
        controller.get().stop(MANAGED_SERVER_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnKillWhenManaged() throws Exception {
        controller.get().kill(MANAGED_SERVER_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnStartWhenNotFound() throws Exception {
        controller.get().start(UNKNOWN_SERVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnStopWhenNotFound() throws Exception {
        controller.get().stop(UNKNOWN_SERVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnKillWhenNotFound() throws Exception {
        controller.get().kill(UNKNOWN_SERVER);
    }

    private void setupAndExecuteManagedDeployment(String containerName) {
        setupAndExecuteDeployment(containerName, true);
    }

    private void setupAndExecuteNonManagedDeployment(String containerName) {
        setupAndExecuteDeployment(containerName, false);
    }

    private void setupAndExecuteManagedUnDeployment(String containerName) {
        setupAndExecuteUnDeployment(containerName, true);
    }

    private void setupAndExecuteNonManagedUnDeployment(String containerName) {
        setupAndExecuteUnDeployment(containerName, false);
    }

    private void setupAndExecuteDeployment(String containerName, boolean managed) {
        DeploymentDescription description = createDeploymentDescription(containerName)
            .shouldBeManaged(managed);

        scenario.get().addDeployment(description);
        controller.get().start(containerName);
        controller.get().start(containerName, new Config().add("managementPort", "19999").map());
    }

    private void setupAndExecuteUnDeployment(String containerName, boolean managed) {
        DeploymentDescription description = createDeploymentDescription(containerName)
            .shouldBeManaged(managed);
        scenario.get().addDeployment(description);
        controller.get().start(containerName);
        scenario.get().deployment(new DeploymentTargetDescription(DEPLOYMENT_NAME)).deployed();

        controller.get().stop(containerName);
    }

    private DeploymentDescription createDeploymentDescription(String targetName) {
        return new DeploymentDescription(DEPLOYMENT_NAME, ShrinkWrap.create(JavaArchive.class))
            .setTarget(new TargetDescription(targetName));
    }
}