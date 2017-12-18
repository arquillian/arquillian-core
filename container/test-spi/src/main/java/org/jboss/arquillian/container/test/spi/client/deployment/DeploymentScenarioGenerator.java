/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.test.spi.client.deployment;

import java.util.List;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.test.spi.TestClass;

/**
 * Extension point for extracting {@link DeploymentDescription}'s from a {@link TestClass}
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface DeploymentScenarioGenerator {
    /**
     * Extract all meta data related to a {@link TestClass}.
     *
     * @param testClass
     *     Data to extract meta data based on.
     *
     * @return The given {@link TestClass}s {@link DeploymentDescription}s
     */
    List<DeploymentDescription> generate(TestClass testClass);
}
