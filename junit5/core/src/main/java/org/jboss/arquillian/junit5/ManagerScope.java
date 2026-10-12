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

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * How widely a single Arquillian {@code Manager} is shared across a JUnit Jupiter run.
 *
 * @see JUnitJupiterTestClassLifecycleManager#MANAGER_SCOPE_PROPERTY
 */
enum ManagerScope {

    /**
     * One manager for the whole run.
     */
    SUITE,

    /**
     * One manager per top-level test class, which is what makes parallel class execution possible.
     *
     * <p>Experimental, and supported only by some container types.</p>
     */
    CLASS;

    static final ManagerScope DEFAULT = SUITE;

    /**
     * Resolves a configured value, as supplied by
     * {@link org.junit.jupiter.api.extension.ExtensionContext#getConfigurationParameter(String)}.
     *
     * <p>Only an absent parameter means "use the default". A parameter that is set but blank is rejected like any
     * other unusable value: it was configured on purpose, and silently running suite-scoped when class scope was
     * meant fails later in ways that point nowhere near the configuration.</p>
     *
     * @param configuredValue the raw value, absent if the parameter was not set
     *
     * @return the matching scope, or {@link #DEFAULT} if the parameter was not set
     */
    static ManagerScope parse(Optional<String> configuredValue) {
        return configuredValue
            .map(String::trim)
            .map(ManagerScope::of)
            .orElse(DEFAULT);
    }

    private static ManagerScope of(String value) {
        for (ManagerScope scope : values()) {
            if (scope.name().equalsIgnoreCase(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException(String.format(
            "Unknown value '%s' for the JUnit configuration parameter %s. Accepted values are: %s.",
            value, JUnitJupiterTestClassLifecycleManager.MANAGER_SCOPE_PROPERTY, acceptedValues()));
    }

    private static String acceptedValues() {
        return Stream.of(values())
            .map(scope -> scope.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.joining(", "));
    }
}
