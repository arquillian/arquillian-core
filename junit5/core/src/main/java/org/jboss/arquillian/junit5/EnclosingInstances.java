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

import java.util.List;

/**
 * The instances of the classes enclosing a {@link org.junit.jupiter.api.Nested} test class, outermost first, held for
 * the test instance they belong to.
 *
 * This is a holder rather than the {@link EnclosingInstancesProducerEvent} itself because setting a value on an
 * {@link org.jboss.arquillian.core.api.InstanceProducer} fires it as an event, which would re-notify the observer
 * storing it.
 *
 * @author Radoslav Husar
 */
final class EnclosingInstances {

    private final Object testInstance;

    private final List<Object> instances;

    private boolean beforeHandled;

    EnclosingInstances(final Object testInstance, final List<Object> instances) {
        this.testInstance = testInstance;
        this.instances = instances;
    }

    Object getTestInstance() {
        return testInstance;
    }

    List<Object> getInstances() {
        return instances;
    }

    boolean isBeforeHandled() {
        return beforeHandled;
    }

    void setBeforeHandled() {
        beforeHandled = true;
    }
}
