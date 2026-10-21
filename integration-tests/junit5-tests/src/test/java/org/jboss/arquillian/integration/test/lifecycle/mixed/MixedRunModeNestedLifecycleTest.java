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
package org.jboss.arquillian.integration.test.lifecycle.mixed;

import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.CLIENT;
import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.SERVER;
import static org.jboss.arquillian.integration.test.lifecycle.api.FileWriterExtension.appendToFile;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceStep;
import org.jboss.arquillian.integration.test.lifecycle.api.AbstractLifecycleTest;
import org.jboss.arquillian.integration.test.lifecycle.api.ArquillianIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;

/**
 * Verifies that {@link RunAsClient} on a {@code @Nested} class applies to its tests and to the classes nested within
 * it, together with the lifecycle methods of all enclosing classes. Every test method shares the same name, which the
 * in-container runner must tell apart by class.
 *
 * @author Radoslav Husar
 */
@ArquillianIntegrationTest({
        @TraceStep(name = "before_all", runsWhere = CLIENT, order = 0),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 1),
        @TraceStep(name = "test_outer", runsWhere = SERVER, order = 2),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 3),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 4),
        @TraceStep(name = "server_before_each", runsWhere = SERVER, order = 5),
        @TraceStep(name = "test_server_nested", runsWhere = SERVER, order = 6),
        @TraceStep(name = "server_after_each", runsWhere = SERVER, order = 7),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 8),
        @TraceStep(name = "before_each", runsWhere = CLIENT, order = 9),
        @TraceStep(name = "client_before_each", runsWhere = CLIENT, order = 10),
        @TraceStep(name = "test_client_nested", runsWhere = CLIENT, order = 11),
        @TraceStep(name = "client_after_each", runsWhere = CLIENT, order = 12),
        @TraceStep(name = "after_each", runsWhere = CLIENT, order = 13),
        @TraceStep(name = "before_each", runsWhere = CLIENT, order = 14),
        @TraceStep(name = "client_before_each", runsWhere = CLIENT, order = 15),
        @TraceStep(name = "test_client_deeply_nested", runsWhere = CLIENT, order = 16),
        @TraceStep(name = "client_after_each", runsWhere = CLIENT, order = 17),
        @TraceStep(name = "after_each", runsWhere = CLIENT, order = 18),
        @TraceStep(name = "after_all", runsWhere = CLIENT, order = 19),
})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class MixedRunModeNestedLifecycleTest extends AbstractLifecycleTest {

    @BeforeAll
    static void beforeAll() {
        appendToFile("before_all");
    }

    @BeforeEach
    void beforeEach() {
        appendToFile("before_each");
    }

    @Test
    void test() {
        appendToFile("test_outer");
    }

    @AfterEach
    void afterEach() {
        appendToFile("after_each");
    }

    @AfterAll
    static void afterAll() {
        appendToFile("after_all");
    }

    @Nested
    @Order(1)
    class ServerTest {

        @BeforeEach
        void serverBeforeEach() {
            appendToFile("server_before_each");
        }

        @Test
        void test() {
            appendToFile("test_server_nested");
        }

        @AfterEach
        void serverAfterEach() {
            appendToFile("server_after_each");
        }
    }

    @Nested
    @Order(2)
    @RunAsClient
    class ClientTest {

        @BeforeEach
        void clientBeforeEach() {
            appendToFile("client_before_each");
        }

        @Test
        void test() {
            appendToFile("test_client_nested");
        }

        @AfterEach
        void clientAfterEach() {
            appendToFile("client_after_each");
        }

        @Nested
        class DeeplyNestedTest {

            @Test
            void test() {
                appendToFile("test_client_deeply_nested");
            }
        }
    }
}
