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

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A template container that fails, because its argument source throws, followed by the class container aborting, so
 * that the abort finishes after the failure.
 *
 * @author Radoslav Husar
 */
public class ClassWithFailedTemplateAndAbortInAfterAll {

    public static final String SOURCE_FAILURE = "Argument source failed";

    @AfterAll
    public static void abort() {
        Assumptions.assumeTrue(false, "Class level assumption in @AfterAll");
    }

    static Stream<String> arguments() {
        throw new IllegalStateException(SOURCE_FAILURE);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void parameterizedTest(String param) {
    }
}
