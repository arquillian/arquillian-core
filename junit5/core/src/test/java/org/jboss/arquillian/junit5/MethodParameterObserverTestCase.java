/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.junit5;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentTargetDescription;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.event.enrichment.BeforeEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.EnrichmentEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MethodParameterObserverTestCase {
    @Mock
    private Instance<ServiceLoader> serviceLoader;
    @Mock
    private Event<EnrichmentEvent> enrichmentEvent;
    @Mock
    private InstanceProducer<MethodParameters> methodParametersProducer;
    @Mock
    private Instance<ContainerContext> containerContextInstance;
    @Mock
    private Instance<DeploymentContext> deploymentContext;
    @Mock
    private Instance<DeploymentScenario> deploymentScenario;
    @InjectMocks
    private MethodParameterObserver observer;

    @Test
    public void shouldSelectNamedDeploymentBeforeEnrichment() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            when(containerContextInstance.get()).thenReturn(mock(ContainerContext.class));
            when(deploymentContext.get()).thenReturn(mock(DeploymentContext.class));
            when(deploymentScenario.get()).thenReturn(mock(DeploymentScenario.class));
            when(serviceLoader.get()).thenReturn(mock(ServiceLoader.class));
            when(serviceLoader.get().all(TestEnricher.class)).thenReturn(Collections.emptyList());
            when(containerContextInstance.get().isActive()).thenReturn(true);
            Deployment selected = mock(Deployment.class);
            when(selected.isDeployed()).thenReturn(true);
            when(deploymentScenario.get().deployment(new DeploymentTargetDescription("second"))).thenReturn(selected);

            observer.injectParameters(new BeforeTestExecutionEvent(new TestCase(),
                TestCase.class.getDeclaredMethod("named")));

            InOrder order = inOrder(deploymentContext.get(), enrichmentEvent);
            order.verify(deploymentContext.get()).activate(selected);
            order.verify(enrichmentEvent).fire(any(BeforeEnrichment.class));
            verify(deploymentScenario.get(), never()).deployments();
        }
    }

    private static class TestCase {
        @OperateOnDeployment("second")
        void named() {
        }
    }
}
