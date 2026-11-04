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

import org.jboss.arquillian.container.spi.ContainerLifecycleListener;
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
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Manager;

/**
 * Adaptor that bridges per-container notification events to the {@link ContainerLifecycleListener} SPI.
 *
 * @author Arquillian
 */
public class ContainerLifecycleListenerAdaptor {

    @Inject
    private Instance<Manager> manager;

    public void onBeforeSetup(@Observes BeforeSetup event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.beforeSetup(event.getDeployableContainer());
        }
    }

    public void onAfterSetup(@Observes AfterSetup event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.afterSetup(event.getDeployableContainer());
        }
    }

    public void onBeforeStart(@Observes BeforeStart event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.beforeStart(event.getDeployableContainer());
        }
    }

    public void onAfterStart(@Observes AfterStart event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.afterStart(event.getDeployableContainer());
        }
    }

    public void onBeforeStop(@Observes BeforeStop event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.beforeStop(event.getDeployableContainer());
        }
    }

    public void onAfterStop(@Observes AfterStop event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.afterStop(event.getDeployableContainer());
        }
    }

    public void onBeforeKill(@Observes BeforeKill event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.beforeKill(event.getDeployableContainer());
        }
    }

    public void onAfterKill(@Observes AfterKill event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.afterKill(event.getDeployableContainer());
        }
    }

    public void onBeforeDeploy(@Observes BeforeDeploy event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.beforeDeploy(event.getDeployableContainer(), event.getDeployment());
        }
    }

    public void onAfterDeploy(@Observes AfterDeploy event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.afterDeploy(event.getDeployableContainer(), event.getDeployment());
        }
    }

    public void onBeforeUndeploy(@Observes BeforeUnDeploy event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.beforeUndeploy(event.getDeployableContainer(), event.getDeployment());
        }
    }

    public void onAfterUndeploy(@Observes AfterUnDeploy event) throws Exception {
        for (ContainerLifecycleListener listener : manager.get().getListeners(ContainerLifecycleListener.class)) {
            listener.afterUndeploy(event.getDeployableContainer(), event.getDeployment());
        }
    }
}
