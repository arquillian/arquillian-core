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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.integration.test.lifecycle.api.AbstractLifecycleTest;
import org.jboss.arquillian.integration.test.lifecycle.api.ArquillianIntegrationTest;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceStep;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.jboss.arquillian.integration.test.lifecycle.api.FileWriterExtension.appendToFile;
import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.CLIENT;
import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.SERVER;

@Disabled("https://github.com/arquillian/arquillian-core/issues/772")
@ArquillianIntegrationTest({
        @TraceStep(name = "before_all_server", runsWhere = SERVER, order = 0),
        @TraceStep(name = "before_all_client", runsWhere = CLIENT, order = 0),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 1),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 1),
        @TraceStep(name = "test_server_one", runsWhere = SERVER, order = 2),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 3),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 3),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 4),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 4),
        @TraceStep(name = "test_server_two", runsWhere = SERVER, order = 5),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 6),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 6),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 7),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 7),
        @TraceStep(name = "test_client_one", runsWhere = CLIENT, order = 8),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 9),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 9),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 10),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 10),
        @TraceStep(name = "test_client_two", runsWhere = CLIENT, order = 11),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 12),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 12),
        @TraceStep(name = "inner_before_all_server", runsWhere = SERVER, order = 13),
        @TraceStep(name = "inner_before_all_client", runsWhere = CLIENT, order = 13),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 14),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 14),
        @TraceStep(name = "inner_before_each_server", runsWhere = SERVER, order = 15),
        @TraceStep(name = "inner_before_each_client", runsWhere = CLIENT, order = 15),
        @TraceStep(name = "nested_test_server", runsWhere = SERVER, order = 16),
        @TraceStep(name = "inner_after_each_server", runsWhere = SERVER, order = 17),
        @TraceStep(name = "inner_after_each_client", runsWhere = CLIENT, order = 17),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 18),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 18),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 19),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 19),
        @TraceStep(name = "inner_before_each_server", runsWhere = SERVER, order = 20),
        @TraceStep(name = "inner_before_each_client", runsWhere = CLIENT, order = 20),
        @TraceStep(name = "nested_test_client", runsWhere = CLIENT, order = 21),
        @TraceStep(name = "inner_after_each_server", runsWhere = SERVER, order = 22),
        @TraceStep(name = "inner_after_each_client", runsWhere = CLIENT, order = 22),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 23),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 23),
        @TraceStep(name = "inner_after_all_server", runsWhere = SERVER, order = 24),
        @TraceStep(name = "inner_after_all_client", runsWhere = CLIENT, order = 24),
        @TraceStep(name = "after_all_client", runsWhere = CLIENT, order = 25),
        @TraceStep(name = "after_all_server", runsWhere = SERVER, order = 25),
})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MixedRunModeLifecycleTest extends AbstractLifecycleTest {

    @BeforeAll
    static void beforeAllServer() {
        appendToFile("before_all_server");
    }

    @BeforeAll
    @RunAsClient
    static void beforeAllClient() {
        appendToFile("before_all_client");
    }

    @BeforeEach
    void beforeEachServer() {
        appendToFile("before_each_server");
    }

    @BeforeEach
    @RunAsClient
    void beforeEachClient() {
        appendToFile("before_each_client");
    }

    @Test
    @Order(1)
    void testServerOne() {
        appendToFile("test_server_one");
    }

    @Test
    @Order(2)
    void testServerTwo() {
        appendToFile("test_server_two");
    }

    @Test
    @RunAsClient
    @Order(3)
    void testClientOne() {
        appendToFile("test_client_one");
    }

    @Test
    @RunAsClient
    @Order(4)
    void testClientTwo() {
        appendToFile("test_client_two");
    }

    @AfterEach
    @RunAsClient
    void afterEachClient() {
        appendToFile("after_each_client");
    }

    @AfterEach
    void afterEachServer() {
        appendToFile("after_each_server");
    }

    @AfterAll
    @RunAsClient
    static void afterAllClient() {
        appendToFile("after_all_client");
    }

    @AfterAll
    static void afterAllServer() {
        appendToFile("after_all_server");
    }

    @Nested
    @Order(5)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class InnerTest {

        @BeforeAll
        void innerBeforeAllServer() {
            appendToFile("inner_before_all_server");
        }

        @BeforeAll
        @RunAsClient
        void innerBeforeAllClient() {
            appendToFile("inner_before_all_client");
        }

        @BeforeEach
        void innerBeforeEachServer() {
            appendToFile("inner_before_each_server");
        }

        @BeforeEach
        @RunAsClient
        void innerBeforeEachClient() {
            appendToFile("inner_before_each_client");
        }

        @Test
        @Order(1)
        void nestedTestServer() {
            appendToFile("nested_test_server");
        }

        @Test
        @RunAsClient
        @Order(2)
        void nestedTestClient() {
            appendToFile("nested_test_client");
        }

        @AfterEach
        @RunAsClient
        void innerAfterEachClient() {
            appendToFile("inner_after_each_client");
        }

        @AfterEach
        void innerAfterEachServer() {
            appendToFile("inner_after_each_server");
        }

        @AfterAll
        @RunAsClient
        void innerAfterAllClient() {
            appendToFile("inner_after_all_client");
        }

        @AfterAll
        void innerAfterAllServer() {
            appendToFile("inner_after_all_server");
        }
    }
}
