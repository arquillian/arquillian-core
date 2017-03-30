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
package org.jboss.arquillian.container.impl.client;

import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.container.spi.event.ContainerControlEvent;
import org.jboss.arquillian.container.spi.event.DeploymentEvent;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;

/**
 * Activates and DeActivates the Container and Deployment contexts.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerDeploymentContextHandler {
    @Inject
    private Instance<ContainerContext> containerContext;

    @Inject
    private Instance<DeploymentContext> deploymentContext;

    /*
     * Container Level
     *
     * Activate ContainerContext on all Container Events
     *
     */
    public void createContainerContext(@Observes EventContext<ContainerControlEvent> context) {
        ContainerContext containerContext = this.containerContext.get();
        ContainerControlEvent event = context.getEvent();

        try {
            containerContext.activate(event.getContainerName());
            context.proceed();
        } finally {
            containerContext.deactivate();
        }
    }

    /*
     * Deployment Level
     *
     * Activate DeploymentContext on all Deployment Events
     */
    public void createDeploymentContext(@Observes EventContext<DeploymentEvent> context) {
        DeploymentContext deploymentContext = this.deploymentContext.get();
        try {
            DeploymentEvent event = context.getEvent();
            deploymentContext.activate(event.getDeployment());

            context.proceed();
        } finally {
            deploymentContext.deactivate();
        }
    }
}
