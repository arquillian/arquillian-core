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
package org.jboss.arquillian.integration.test.lifecycle.server;

import org.jboss.arquillian.integration.test.lifecycle.api.AbstractLifecycleTest;
import org.jboss.arquillian.integration.test.lifecycle.api.ArquillianIntegrationTest;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceStep;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestMethodOrder;

import static org.jboss.arquillian.integration.test.lifecycle.api.FileWriterExtension.appendToFile;
import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.SERVER;

@Disabled("https://github.com/arquillian/arquillian-core/issues/771,https://github.com/arquillian/arquillian-core/issues/772")
@ArquillianIntegrationTest({
        @TraceStep(name = "before_all", runsWhere = SERVER, order = 0),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 1),
        @TraceStep(name = "repeated_test_1", runsWhere = SERVER, order = 2),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 3),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 4),
        @TraceStep(name = "repeated_test_1", runsWhere = SERVER, order = 5),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 6),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 7),
        @TraceStep(name = "repeated_test_1", runsWhere = SERVER, order = 8),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 9),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 10),
        @TraceStep(name = "repeated_test_2", runsWhere = SERVER, order = 11),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 12),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 13),
        @TraceStep(name = "repeated_test_2", runsWhere = SERVER, order = 14),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 15),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 16),
        @TraceStep(name = "repeated_test_2", runsWhere = SERVER, order = 17),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 18),
        @TraceStep(name = "after_all", runsWhere = SERVER, order = 19),
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServerSideRepeatedTestTest extends AbstractLifecycleTest {

    @Deployment
    static JavaArchive createDeployment() {
        return createBaseDeployment();
    }

    @BeforeAll
    static void beforeAll() {
        appendToFile("before_all");
    }

    @BeforeEach
    void beforeEach() {
        appendToFile("before_each");
    }

    @Order(1)
    @RepeatedTest(3)
    void repeatedTest() {
        appendToFile("repeated_test_1");
    }

    @Order(2)
    @RepeatedTest(3)
    void anotherRepeatedTest() {
        appendToFile("repeated_test_2");
    }

    @AfterEach
    void afterEach() {
        appendToFile("after_each");
    }

    @AfterAll
    static void afterAll() {
        appendToFile("after_all");
    }

}
