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

package org.jboss.arquillian.integration.test.cdi;

import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.integration.test.common.app.Greeter;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verifies that fields of the classes enclosing a {@code @Nested} test class are injected, both for their own
 * {@code @BeforeEach} and {@code @AfterEach} methods and for the nested classes using them. The test methods share a
 * name across the nesting levels, which the in-container runner must tell apart by class.
 *
 * @author Radoslav Husar
 */
@ArquillianTest
public class NestedCdiInjectionTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(Greeter.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    Greeter greeter;

    @BeforeEach
    public void checkGreeter() {
        Assertions.assertNotNull(greeter, "Greeter should have been injected before @BeforeEach");
    }

    @Test
    public void validateGreeting() {
        Assertions.assertEquals("Hello JUnit5!", greeter.greet("JUnit5"));
    }

    @AfterEach
    public void checkGreeterAfterEach() {
        Assertions.assertNotNull(greeter, "Greeter should have been injected for @AfterEach");
    }

    @Nested
    class NestedTest {

        @Inject
        Greeter nestedGreeter;

        @BeforeEach
        public void checkNestedGreeter() {
            Assertions.assertNotNull(greeter, "Greeter should have been injected before the nested @BeforeEach");
            Assertions.assertNotNull(nestedGreeter, "Nested Greeter should have been injected before @BeforeEach");
        }

        @Test
        public void validateGreeting() {
            Assertions.assertEquals("Hello Nested!", greeter.greet("Nested"));
            Assertions.assertEquals("Hello Nested!", nestedGreeter.greet("Nested"));
        }

        @Nested
        class DeeplyNestedTest {

            @Test
            public void validateGreeting() {
                Assertions.assertEquals("Hello Deeply Nested!", greeter.greet("Deeply Nested"));
                Assertions.assertEquals("Hello Deeply Nested!", nestedGreeter.greet("Deeply Nested"));
            }
        }
    }
}
