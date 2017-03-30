/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.util.Collection;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class RemoteResourceCommandObserver {

    public void lookup(@Observes RemoteResourceCommand command, ServiceLoader serviceLoader) {
        Collection<ResourceProvider> resourceProviders = serviceLoader.all(ResourceProvider.class);
        Class<?> type = command.getType();
        for (ResourceProvider resourceProvider : resourceProviders) {
            if (resourceProvider.canProvide(type)) {
                Object value = resourceProvider.lookup(command.getResource(), command.getAnnotations());
                if (value == null) {
                    throw new RuntimeException(
                        "Provider for type " + type + " returned a null value: " + resourceProvider);
                }
                command.setResult(value);
                return;
            }
        }
        throw new IllegalArgumentException("No ResourceProvider found for type: " + type);
    }
}
