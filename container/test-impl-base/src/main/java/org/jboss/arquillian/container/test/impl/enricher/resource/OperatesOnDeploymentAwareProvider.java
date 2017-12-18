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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.lang.annotation.Annotation;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentTargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * OperatesOnDeploymentAwareProvider
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class OperatesOnDeploymentAwareProvider implements ResourceProvider {
    @Inject
    private Instance<ContainerContext> containerContext;

    @Inject
    private Instance<DeploymentContext> deploymentContext;

    @Inject
    private Instance<DeploymentScenario> deploymentScenario;

    @Inject
    private Instance<ContainerRegistry> containerRegistry;

    public abstract Object doLookup(ArquillianResource resource, Annotation... qualifiers);

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return runInDeploymentContext(resource, qualifiers);
    }

    private Object runInDeploymentContext(ArquillianResource resource, Annotation... qualifiers) {
        DeploymentContext context = null;
        DeploymentScenario scenario = null;
        boolean activateContext = containsOperatesOnDeployment(qualifiers);
        boolean contextActivated = false;
        try {
            Deployment deployment = null;
            if (activateContext) {
                context = deploymentContext.get();
                scenario = deploymentScenario.get();
                if (scenario == null) {
                    throw new IllegalStateException("No "
                        + DeploymentScenario.class.getSimpleName()
                        + " found. "
                        +
                        "Possible cause, @"
                        + OperateOnDeployment.class.getSimpleName()
                        + " is currently only supported on the client side. (@"
                        + RunAsClient.class.getSimpleName()
                        + ")");
                }
                OperateOnDeployment operatesOn = getOperatesOnDeployment(qualifiers);
                deployment = scenario.deployment(new DeploymentTargetDescription(operatesOn.value()));
                if (deployment == null) {
                    throw new IllegalArgumentException(
                        "Could not operate on deployment (@" + OperateOnDeployment.class.getSimpleName() + "), " +
                            "no deployment found with name: " + operatesOn.value());
                }
                context.activate(deployment);
                contextActivated = true;
            }
            return runInContainerContext(deployment == null ? null : deployment.getDescription().getTarget(), resource,
                qualifiers);
        } finally {
            if (contextActivated) {
                context.deactivate();
            }
        }
    }

    private Object runInContainerContext(TargetDescription targetDescription, ArquillianResource resource,
        Annotation... qualifiers) {
        ContainerContext context = null;
        ContainerRegistry registry = null;
        boolean activateContext = targetDescription != null;
        boolean contextActivated = false;
        try {
            if (activateContext) {
                context = containerContext.get();
                registry = containerRegistry.get();
                if (registry == null) {
                    throw new IllegalStateException("No "
                        + ContainerRegistry.class.getSimpleName()
                        + " found. "
                        +
                        "Possible problem is, @"
                        + OperateOnDeployment.class.getSimpleName()
                        + " is currently only supported on the client side.");
                }
                Container container = registry.getContainer(targetDescription);
                if (container == null) {
                    throw new IllegalArgumentException(
                        "Could not operate on deployment (@" + OperateOnDeployment.class.getSimpleName() + "), " +
                            "no container found with name: " + targetDescription);
                }
                context.activate(container.getName());
                contextActivated = true;
            }
            return doLookup(resource, qualifiers);
        } finally {
            if (contextActivated) {
                context.deactivate();
            }
        }
    }

    public boolean containsOperatesOnDeployment(Annotation[] qualifiers) {
        return getOperatesOnDeployment(qualifiers) != null;
    }

    private OperateOnDeployment getOperatesOnDeployment(Annotation[] qualifiers) {
        if (qualifiers != null) {
            for (Annotation annotation : qualifiers) {
                if (annotation.annotationType() == OperateOnDeployment.class) {
                    return OperateOnDeployment.class.cast(annotation);
                }
            }
        }
        return null;
    }
}
