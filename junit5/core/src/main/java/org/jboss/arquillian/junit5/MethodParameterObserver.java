/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.event.enrichment.AfterEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.BeforeEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.EnrichmentEvent;

/**
 * The observer used to process method parameters provided by Arquillian.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MethodParameterObserver {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<EnrichmentEvent> enrichmentEvent;

    @Inject
    @TestScoped
    private InstanceProducer<MethodParameters> methodParametersProducer;

    @Inject
    private Instance<DeploymentContext> deploymentContext;

    @Inject
    private Instance<DeploymentScenario> deploymentScenario;

    /**
     * Updates the stored {@link MethodParameters} for method parameters which can be provided by Arquillian.
     *
     * @param event the fired event
     */
    public void injectParameters(@Observes final BeforeTestExecutionEvent event) {
        final boolean contextActivated = activateDeployments();
        try {
            final Object testInstance = event.getTestInstance();
            final Method testMethod = event.getTestMethod();
            enrichmentEvent.fire(new BeforeEnrichment(testInstance, testMethod));
            final MethodParameters methodParameters = methodParametersProducer.get();
            final Collection<TestEnricher> testEnrichers = serviceLoader.get().all(TestEnricher.class);
            for (TestEnricher enricher : testEnrichers) {
                final Object[] values = enricher.resolve(testMethod);
                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null) {
                        methodParameters.add(i, values[i]);
                    }
                }
            }
            enrichmentEvent.fire(new AfterEnrichment(testEnrichers, testMethod));
        } finally {
            if (contextActivated) {
                deploymentContext.get().deactivate();
            }
        }
    }

    /**
     * Sets the {@link MethodParameters} instance.
     *
     * @param event the fired event
     */
    public void injectParameters(@Observes MethodParameterProducerEvent event) {
        methodParametersProducer.set(event.getTestParameterHolder());
    }

    private boolean activateDeployments() {
        final DeploymentContext context = deploymentContext.get();
        // If the deployment context is not available or already active, we don't need to activate the deployment context
        if (context == null || context.isActive()) {
            return false;
        }
        final Collection<Deployment> activeDeployments = deploymentScenario.get().deployments().stream()
            .filter(Deployment::isDeployed)
            .collect(Collectors.toList());
        // If there are multiple deployments, don't activate any of them as an @OperatesOnDeployment should be used
        if (activeDeployments.size() != 1) {
            return false;
        }
        activeDeployments.forEach(context::activate);
        return true;
    }
}
