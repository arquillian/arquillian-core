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

import org.jboss.arquillian.container.spi.ContainerMultiControlListener;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.container.spi.event.StartClassContainers;
import org.jboss.arquillian.container.spi.event.StartSuiteContainers;
import org.jboss.arquillian.container.spi.event.StopClassContainers;
import org.jboss.arquillian.container.spi.event.StopManualContainers;
import org.jboss.arquillian.container.spi.event.StopSuiteContainers;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Manager;

/**
 * Adaptor that bridges multi-container control events to the {@link ContainerMultiControlListener} SPI.
 *
 * @author Arquillian
 */
public class ContainerMultiControlListenerAdaptor {

    @Inject
    private Instance<Manager> manager;

    public void onSetupContainers(@Observes SetupContainers event) throws Exception {
        for (ContainerMultiControlListener listener : manager.get().getListeners(ContainerMultiControlListener.class)) {
            listener.setupContainers();
        }
    }

    public void onStartSuiteContainers(@Observes StartSuiteContainers event) throws Exception {
        for (ContainerMultiControlListener listener : manager.get().getListeners(ContainerMultiControlListener.class)) {
            listener.startSuiteContainers();
        }
    }

    public void onStartClassContainers(@Observes StartClassContainers event) throws Exception {
        for (ContainerMultiControlListener listener : manager.get().getListeners(ContainerMultiControlListener.class)) {
            listener.startClassContainers();
        }
    }

    public void onStopSuiteContainers(@Observes StopSuiteContainers event) throws Exception {
        for (ContainerMultiControlListener listener : manager.get().getListeners(ContainerMultiControlListener.class)) {
            listener.stopSuiteContainers();
        }
    }

    public void onStopClassContainers(@Observes StopClassContainers event) throws Exception {
        for (ContainerMultiControlListener listener : manager.get().getListeners(ContainerMultiControlListener.class)) {
            listener.stopClassContainers();
        }
    }

    public void onStopManualContainers(@Observes StopManualContainers event) throws Exception {
        for (ContainerMultiControlListener listener : manager.get().getListeners(ContainerMultiControlListener.class)) {
            listener.stopManualContainers();
        }
    }

    public void onDeployManagedDeployments(@Observes DeployManagedDeployments event) throws Exception {
        for (ContainerMultiControlListener listener : manager.get().getListeners(ContainerMultiControlListener.class)) {
            listener.deployManagedDeployments();
        }
    }

    public void onUndeployManagedDeployments(@Observes UnDeployManagedDeployments event) throws Exception {
        for (ContainerMultiControlListener listener : manager.get().getListeners(ContainerMultiControlListener.class)) {
            listener.undeployManagedDeployments();
        }
    }
}
