/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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

import org.jboss.arquillian.container.spi.ConfigurationException;

/**
 * Container configuration that can be validated
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 */
public interface ContainerConfiguration {

    /**
     * Validates if current configuration is valid, that is if all required
     * properties are set and have correct values
     */
    void validate() throws ConfigurationException;
}
