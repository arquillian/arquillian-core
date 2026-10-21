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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.junit5;

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

/**
 * Enriches the instances of the classes enclosing a {@link org.junit.jupiter.api.Nested} test class.
 *
 * Core enriches only the test instance passed in the {@link org.jboss.arquillian.test.spi.event.suite.Before} event,
 * which for a {@link org.junit.jupiter.api.Nested} test is the innermost instance. Fields of the enclosing classes,
 * used by their {@link org.junit.jupiter.api.BeforeEach} methods or by the nested test through the outer instance,
 * would be left unset. Enriching them once core reports that it enriched the test instance means they are enriched
 * exactly when it is, so the run mode decisions made in core apply to them as well.
 *
 * @author Radoslav Husar
 */
public class EnclosingInstancesEnricher {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<EnrichmentEvent> enrichmentEvent;

    @Inject
    @TestScoped
    private InstanceProducer<EnclosingInstances> enclosingInstancesProducer;

    /**
     * Stores the enclosing instances for the test instance they belong to.
     *
     * @param event the fired EnclosingInstancesProducerEvent with enclosing instances
     */
    public void storeEnclosingInstances(@Observes EnclosingInstancesProducerEvent event) {
        enclosingInstancesProducer.set(event.getEnclosingInstances());
    }

    /**
     * Enriches the enclosing instances, outermost first, after the test instance they belong to was enriched.
     *
     * @param event the fired event
     */
    public void enrichEnclosingInstances(@Observes AfterEnrichment event) {
        final EnclosingInstances enclosingInstances = enclosingInstancesProducer.get();
        // Only react to the enrichment of the test instance itself, not to that of the enclosing instances fired below
        // or of method parameters
        if (enclosingInstances == null || enclosingInstances.getTestInstance() != event.getInstance()) {
            return;
        }
        final Collection<TestEnricher> testEnrichers = serviceLoader.get().all(TestEnricher.class);
        for (Object instance : enclosingInstances.getInstances()) {
            enrichmentEvent.fire(new BeforeEnrichment(instance, event.getMethod()));
            for (TestEnricher enricher : testEnrichers) {
                enricher.enrich(instance);
            }
            enrichmentEvent.fire(new AfterEnrichment(instance, event.getMethod()));
        }
    }
}
