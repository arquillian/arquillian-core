/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit5.container;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * JUnitJupiterDeploymentAppenderTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JUnitJupiterDeploymentAppenderTestCase {

  @Test
  public void shouldGenerateDependencies() throws Exception {
    Archive<?> archive = new JUnitJupiterDeploymentAppender().createAuxiliaryArchive();

    Assertions.assertTrue(
        archive.contains(ArchivePaths.create("/META-INF/services/org.jboss.arquillian.container.test.spi.TestRunner")),
        "Should have added Extension");

    Assertions.assertTrue(
        archive.contains(ArchivePaths.create("/org/jboss/arquillian/junit5/container/JUnitJupiterTestRunner.class")),
        "Should have added TestRunner Impl");
  }
}
