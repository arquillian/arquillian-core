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
package org.jboss.arquillian.test.impl;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.test.spi.TestEnrichmentListener;
import org.jboss.arquillian.test.spi.event.enrichment.AfterEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.BeforeEnrichment;

/**
 * Adaptor that bridges test enrichment events to the {@link TestEnrichmentListener} SPI.
 *
 * @author Arquillian
 */
public class TestEnrichmentListenerAdaptor {

    @Inject
    private Instance<Manager> manager;

    public void onBeforeEnrichment(@Observes BeforeEnrichment event) throws Exception {
        for (TestEnrichmentListener listener : manager.get().getListeners(TestEnrichmentListener.class)) {
            listener.beforeEnrichment(event.getInstance(), event.getMethod());
        }
    }

    public void onAfterEnrichment(@Observes AfterEnrichment event) throws Exception {
        for (TestEnrichmentListener listener : manager.get().getListeners(TestEnrichmentListener.class)) {
            listener.afterEnrichment(event.getInstance(), event.getMethod());
        }
    }
}
