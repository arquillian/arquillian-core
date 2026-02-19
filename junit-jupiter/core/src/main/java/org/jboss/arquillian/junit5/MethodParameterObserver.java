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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
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
    private static final AutoCloseable EMPTY = () -> {
    };

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<EnrichmentEvent> enrichmentEvent;

    @Inject
    @TestScoped
    private InstanceProducer<MethodParameters> methodParametersProducer;

    @Inject
    private Instance<ContainerContext> containerContextInstance;

    @Inject
    private Instance<ContainerRegistry> containerRegistry;

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
        // Some contexts may need to be activated and deactivated before we lookup the parameters
        try (AutoCloseable ignore = activateContexts()) {
            final Object testInstance = event.getTestInstance();
            final Method testMethod = event.getTestMethod();
            enrichmentEvent.fire(new BeforeEnrichment(testInstance, testMethod));
            final MethodParameters methodParameters = methodParametersProducer.get();
            final Collection<TestEnricher> testEnrichers = serviceLoader.get().all(TestEnricher.class);
            for (TestEnricher enricher : testEnrichers) {
                final Object[] values = enricher.resolve(testMethod);
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        if (values[i] != null) {
                            methodParameters.add(i, values[i]);
                        }
                    }
                }
            }
            enrichmentEvent.fire(new AfterEnrichment(testEnrichers, testMethod));
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Failed inject parameters", e);
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

    /**
     * The {@link BeforeTestExecutionEvent} is fired after the {@link org.jboss.arquillian.test.spi.TestRunnerAdaptor#before(Object, Method, LifecycleMethodExecutor)}
     * is executed. There are some contexts we will need to activate before we attempt to lookup the resource values
     * for parameters.
     *
     * @return an auto-closeable which deactivates any contexts that were activated
     */
    private AutoCloseable activateContexts() {
        final DeploymentContext deploymentContext = this.deploymentContext.get();
        final ContainerContext containerContext = this.containerContextInstance.get();
        // If the deployment context and the container contexts are not available or already active, we don't need to
        // activate anything.
        if ((deploymentContext == null || deploymentContext.isActive()) && (containerContext == null || containerContext.isActive())) {
            return EMPTY;
        }
        // Get a list of the deployed deployments
        final List<Deployment> activeDeployments = deploymentScenario.get().deployments().stream()
            .filter(Deployment::isDeployed)
            .collect(Collectors.toList());

        // Activate any container contexts we may need
        final AutoCloseable activeContainerContexts = activateContainerContexts(containerContext, activeDeployments);
        // If the deployment context is already activated or not available, we don't need to activate it. If there are
        // multiple deployments, don't activate any of them as an @OperatesOnDeployment should be used.
        if (deploymentContext == null || deploymentContext.isActive() || activeDeployments.size() != 1) {
            return activeContainerContexts;
        }
        // We should only have one deployment
        final Deployment activeDeployment = activeDeployments.get(0);
        deploymentContext.activate(activeDeployment);
        return () -> {
            RuntimeException error = null;
            try {
                deploymentContext.deactivate();
            } catch (Throwable throwable) {
                error = new RuntimeException("Failed to deactivate deployment context: " + deploymentContext.getActiveId(), throwable);
            }
            try {
                activeContainerContexts.close();
            } catch (Throwable throwable) {
                if (error == null) {
                    error = new RuntimeException("Failed to deactivate container context", throwable);
                } else {
                    error.addSuppressed(throwable);
                }
            }
            if (error != null) {
                throw error;
            }
        };
    }

    private AutoCloseable activateContainerContexts(final ContainerContext containerContext, final Collection<Deployment> deployments) {
        if (containerContext == null || containerContext.isActive()) {
            return EMPTY;
        }
        final Deque<ContainerContext> activeContainerContexts = new ArrayDeque<>();
        for (Deployment deployment : deployments) {
            // This should be true as we filter on only deployed deployments, but we'll be safe.
            if (deployment.isDeployed()) {
                final Container<?> container = containerRegistry.get()
                    .getContainer(deployment.getDescription().getTarget());
                if (container != null) {
                    // Only activate started containers
                    if (container.getState() == Container.State.STARTED) {
                        containerContext.activate(container.getName());
                        activeContainerContexts.addLast(containerContext);
                    }
                }
            }
        }
        if (!containerContext.isActive()) {
            // Activate the default container, if it's started and there were no deployments
            final Container<?> container = findDefaultContainer();
            if (container != null && container.getState() == Container.State.STARTED) {
                containerContext.activate(container.getName());
                activeContainerContexts.addLast(containerContext);
            }
        }
        return () -> {
            RuntimeException error = null;
            ContainerContext activeContainerContext;
            while ((activeContainerContext = activeContainerContexts.pollFirst()) != null) {
                try {
                    activeContainerContext.deactivate();
                } catch (Throwable throwable) {
                    if (error == null) {
                        error = new RuntimeException("Failed to deactivate container context: " + activeContainerContext.getActiveId());
                    }
                    error.addSuppressed(throwable);
                }
            }
            if (error != null) {
                throw error;
            }
        };
    }

    private Container<?> findDefaultContainer() {
        final ContainerRegistry containerRegistry = this.containerRegistry.get();
        @SuppressWarnings("rawtypes")
        final List<Container> containers = containerRegistry.getContainers();
        if (containers.size() == 1) {
            return containers.get(0);
        }
        for (Container<?> container : containers) {
            if (container.getContainerConfiguration().isDefault()) {
                return container;
            }
        }
        return null;
    }
}
