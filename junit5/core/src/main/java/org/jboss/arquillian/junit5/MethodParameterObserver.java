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
import org.jboss.arquillian.test.spi.event.suite.Before;

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

    /**
     * Updates the stored {@link MethodParameters} for method parameters which can be provided by Arquillian.
     *
     * @param event the fired event
     */
    public void injectParameters(@Observes final Before event) {
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
    }

    /**
     * Sets the {@link MethodParameters} instance.
     *
     * @param event the fired event
     */
    public void injectParameters(@Observes MethodParameterProducerEvent event) {
        methodParametersProducer.set(event.getTestParameterHolder());
    }
}
