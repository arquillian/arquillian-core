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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.jboss.arquillian.integration.test.lifecycle.api.FileWriterExtension.appendToFile;
import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.SERVER;

@Disabled("https://github.com/arquillian/arquillian-core/issues/771,https://github.com/arquillian/arquillian-core/issues/772")
@ArquillianIntegrationTest({
        @TraceStep(name = "before_all", runsWhere = SERVER, order = 0),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 1),
        @TraceStep(name = "parameterized_test", runsWhere = SERVER, order = 2),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 3),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 4),
        @TraceStep(name = "parameterized_test", runsWhere = SERVER, order = 5),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 6),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 7),
        @TraceStep(name = "parameterized_test", runsWhere = SERVER, order = 8),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 9),
        @TraceStep(name = "after_all", runsWhere = SERVER, order = 10),
})
class ServerSideParameterizedTestTest extends AbstractLifecycleTest {

    @BeforeAll
    static void beforeAll() {
        appendToFile("before_all");
    }

    @BeforeEach
    void beforeEach() {
        appendToFile("before_each");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c"})
    void parameterizedTest(String value) {
        appendToFile("parameterized_test");
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
