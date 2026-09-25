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
package org.jboss.arquillian.container.spi;

import org.jboss.arquillian.container.spi.client.deployment.Deployment;

/**
 * Listener SPI for single-container control events
 * ({@code org.jboss.arquillian.container.spi.event}: SetupContainer, StartContainer,
 * StopContainer, KillContainer, DeployDeployment, UnDeployDeployment).
 *
 * <p>Register instances via {@link org.jboss.arquillian.core.spi.Manager#addListener(Class, Object)}.
 * Each method defaults to a no-op so implementors only override what they need.
 *
 * @author Arquillian
 */
public interface ContainerControlListener {

    default void setupContainer(Container container) throws Exception {
    }

    default void startContainer(Container container) throws Exception {
    }

    default void stopContainer(Container container) throws Exception {
    }

    default void killContainer(Container container) throws Exception {
    }

    default void deployDeployment(Container container, Deployment deployment) throws Exception {
    }

    default void undeployDeployment(Container container, Deployment deployment) throws Exception {
    }
}
