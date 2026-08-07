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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static org.jboss.arquillian.integration.test.lifecycle.api.FileWriterExtension.appendToFile;
import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.CLIENT;
import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.SERVER;

@Disabled("https://github.com/arquillian/arquillian-core/issues/771,https://github.com/arquillian/arquillian-core/issues/772")
@ArquillianIntegrationTest({
        @TraceStep(name = "before_all_server", runsWhere = SERVER, order = 0),
        @TraceStep(name = "before_all_client", runsWhere = CLIENT, order = 0),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 1),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 1),
        @TraceStep(name = "repeated_server", runsWhere = SERVER, order = 2),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 3),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 3),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 4),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 4),
        @TraceStep(name = "repeated_server", runsWhere = SERVER, order = 5),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 6),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 6),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 7),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 7),
        @TraceStep(name = "repeated_server", runsWhere = SERVER, order = 8),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 9),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 9),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 10),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 10),
        @TraceStep(name = "repeated_client", runsWhere = CLIENT, order = 11),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 12),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 12),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 13),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 13),
        @TraceStep(name = "repeated_client", runsWhere = CLIENT, order = 14),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 15),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 15),
        @TraceStep(name = "before_each_server", runsWhere = SERVER, order = 16),
        @TraceStep(name = "before_each_client", runsWhere = CLIENT, order = 16),
        @TraceStep(name = "repeated_client", runsWhere = CLIENT, order = 17),
        @TraceStep(name = "after_each_server", runsWhere = SERVER, order = 18),
        @TraceStep(name = "after_each_client", runsWhere = CLIENT, order = 18),
        @TraceStep(name = "after_all_server", runsWhere = SERVER, order = 19),
        @TraceStep(name = "after_all_client", runsWhere = CLIENT, order = 19),
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MixedRunModeRepeatedTestTest extends AbstractLifecycleTest {

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

    @Order(1)
    @RepeatedTest(3)
    void repeatedServer() {
        appendToFile("repeated_server");
    }

    @Order(2)
    @RepeatedTest(3)
    @RunAsClient
    void repeatedClient() {
        appendToFile("repeated_client");
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
}
