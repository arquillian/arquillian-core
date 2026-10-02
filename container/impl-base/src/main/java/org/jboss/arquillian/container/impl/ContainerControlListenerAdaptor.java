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

import org.jboss.arquillian.container.spi.ContainerControlListener;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.KillContainer;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Manager;

/**
 * Adaptor that bridges single-container control events to the {@link ContainerControlListener} SPI.
 *
 * @author Arquillian
 */
public class ContainerControlListenerAdaptor {

    @Inject
    private Instance<Manager> manager;

    public void onSetupContainer(@Observes SetupContainer event) throws Exception {
        for (ContainerControlListener listener : manager.get().getListeners(ContainerControlListener.class)) {
            listener.setupContainer(event.getContainer());
        }
    }

    public void onStartContainer(@Observes StartContainer event) throws Exception {
        for (ContainerControlListener listener : manager.get().getListeners(ContainerControlListener.class)) {
            listener.startContainer(event.getContainer());
        }
    }

    public void onStopContainer(@Observes StopContainer event) throws Exception {
        for (ContainerControlListener listener : manager.get().getListeners(ContainerControlListener.class)) {
            listener.stopContainer(event.getContainer());
        }
    }

    public void onKillContainer(@Observes KillContainer event) throws Exception {
        for (ContainerControlListener listener : manager.get().getListeners(ContainerControlListener.class)) {
            listener.killContainer(event.getContainer());
        }
    }

    public void onDeployDeployment(@Observes DeployDeployment event) throws Exception {
        for (ContainerControlListener listener : manager.get().getListeners(ContainerControlListener.class)) {
            listener.deployDeployment(event.getContainer(), event.getDeployment());
        }
    }

    public void onUndeployDeployment(@Observes UnDeployDeployment event) throws Exception {
        for (ContainerControlListener listener : manager.get().getListeners(ContainerControlListener.class)) {
            listener.undeployDeployment(event.getContainer(), event.getDeployment());
        }
    }
}
