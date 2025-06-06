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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A simple holder for method parameters resolved by a {@link org.jboss.arquillian.test.spi.TestEnricher} and their
 * index.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class MethodParameters implements AutoCloseable, ExtensionContext.Store.CloseableResource {
    private final Map<Integer, Object> parameters;

    MethodParameters() {
        this.parameters = new ConcurrentHashMap<>();
    }

    /**
     * Adds a parameter value with the given index.
     *
     * @param index the index of the parameter
     * @param value the value for the parameter
     */
    void add(final int index, final Object value) {
        parameters.put(index, value);
    }

    /**
     * Gets the parameter value based on the index.
     *
     * @param index the index of the parameter to get the value for
     *
     * @return the value for the parameter or {@code null} if one was not registered at the provided index
     */
    Object get(final int index) {
        return parameters.get(index);
    }

    @Override
    public void close() {
        parameters.clear();
    }
}
