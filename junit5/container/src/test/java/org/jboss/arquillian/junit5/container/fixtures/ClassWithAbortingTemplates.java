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
package org.jboss.arquillian.junit5.container.fixtures;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Templates where only some invocations abort, so the container must report the abort against the invocation that
 * aborted rather than the template as a whole.
 */
public class ClassWithAbortingTemplates {

    @RepeatedTest(3)
    public void repeatedTest(RepetitionInfo repetitionInfo) {
        Assumptions.assumeTrue(repetitionInfo.getCurrentRepetition() != 2, "Repetition 2 aborted");
    }

    @ParameterizedTest
    @ValueSource(strings = {"one", "two", "three"})
    public void parameterizedTest(String param) {
        Assumptions.assumeTrue(!"two".equals(param), "Invocation two aborted");
    }

    @RepeatedTest(3)
    public void abortingAndFailingTest(RepetitionInfo repetitionInfo) {
        Assumptions.assumeTrue(repetitionInfo.getCurrentRepetition() != 1, "Repetition 1 aborted");
        Assertions.assertNotEquals(2, repetitionInfo.getCurrentRepetition(), "Repetition 2 failed");
    }
}
