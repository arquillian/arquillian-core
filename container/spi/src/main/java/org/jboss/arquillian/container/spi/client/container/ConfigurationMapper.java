/*
 * JBoss, Home of Professional Open Source
 * Copyright 2024 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.spi.client.container;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;

/**
 * An interface that {@link DeployableContainer<T>} can use to control how the mapping
 * from the externalized container definition, as found in an arquillian.xml file
 * for example, is applied to the {@link ContainerConfiguration} instances
 *
 * @param <T> the type of ContainerConfiguration
 */
public interface ConfigurationMapper<T extends ContainerConfiguration> {
    /**
     *
     * @param containerConfiguration - the deployable container configuration instance to populate
     * @param definition - the container definition from the ArquillianDescriptor that
     *                   was parsed
     */
    void populateConfiguration(T containerConfiguration, ContainerDef definition);
}
