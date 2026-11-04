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

import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.CLIENT;
import static org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere.SERVER;
import static org.jboss.arquillian.integration.test.lifecycle.api.FileWriterExtension.appendToFile;
import static org.jboss.arquillian.integration.test.lifecycle.api.TestOutcomeRecorder.Outcome.ABORTED;
import static org.jboss.arquillian.integration.test.lifecycle.api.TestOutcomeRecorder.Outcome.SUCCESSFUL;

import java.util.Map;

import org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceStep;
import org.jboss.arquillian.integration.test.lifecycle.api.AbstractLifecycleTest;
import org.jboss.arquillian.integration.test.lifecycle.api.ArquillianIntegrationTest;
import org.jboss.arquillian.integration.test.lifecycle.api.TestOutcomeRecorder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Verifies that an invocation of a repeated or parameterized test that fails an assumption in the container is
 * reported as aborted, while the other invocations of the same template are still reported as successful.
 */
@ArquillianIntegrationTest({
        @TraceStep(name = "before_all", runsWhere = CLIENT, order = 0),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 1),
        @TraceStep(name = "repeated_test", runsWhere = SERVER, order = 2),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 3),
        // Repetition 2 aborts before tracing anything itself
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 4),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 5),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 6),
        @TraceStep(name = "repeated_test", runsWhere = SERVER, order = 7),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 8),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 9),
        @TraceStep(name = "parameterized_test", runsWhere = SERVER, order = 10),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 11),
        // Invocation "b" aborts before tracing anything itself
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 12),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 13),
        @TraceStep(name = "before_each", runsWhere = SERVER, order = 14),
        @TraceStep(name = "parameterized_test", runsWhere = SERVER, order = 15),
        @TraceStep(name = "after_each", runsWhere = SERVER, order = 16),
        @TraceStep(name = "after_all", runsWhere = CLIENT, order = 17),
})
@ExtendWith(TestOutcomeRecorder.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServerSideAbortedTemplateTest extends AbstractLifecycleTest {

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
    void repeatedTest(RepetitionInfo repetitionInfo) {
        Assumptions.assumeTrue(repetitionInfo.getCurrentRepetition() != 2, "Repetition 2 aborted");
        appendToFile("repeated_test");
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c"})
    void parameterizedTest(String value) {
        Assumptions.assumeTrue(!"b".equals(value), "Invocation b aborted");
        appendToFile("parameterized_test");
    }

    @AfterEach
    void afterEach() {
        appendToFile("after_each");
    }

    @AfterAll
    static void afterAll() {
        appendToFile("after_all");
        if (RunsWhere.isRunningOnClient()) {
            Assertions.assertEquals(Map.of(
                    "repeatedTest repetition 1 of 3", SUCCESSFUL,
                    "repeatedTest repetition 2 of 3", ABORTED,
                    "repeatedTest repetition 3 of 3", SUCCESSFUL,
                    "parameterizedTest [1] a", SUCCESSFUL,
                    "parameterizedTest [2] b", ABORTED,
                    "parameterizedTest [3] c", SUCCESSFUL),
                    TestOutcomeRecorder.outcomes(ServerSideAbortedTemplateTest.class));
        }
    }
}
