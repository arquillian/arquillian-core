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
package org.jboss.arquillian.integration.test.lifecycle.api;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * JUnit Jupiter extension that records the outcome JUnit reported for each test of a class, keyed by the test method
 * name and display name, so that a test can assert how its invocations were reported, e.g. aborted rather than failed.
 */
public class TestOutcomeRecorder implements TestWatcher {

    public enum Outcome {
        SUCCESSFUL, ABORTED, FAILED, DISABLED
    }

    private static final Map<Class<?>, Map<String, Outcome>> OUTCOMES = new ConcurrentHashMap<>();

    @Override
    public void testSuccessful(ExtensionContext context) {
        record(context, Outcome.SUCCESSFUL);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        record(context, Outcome.ABORTED);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        record(context, Outcome.FAILED);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        record(context, Outcome.DISABLED);
    }

    public static Map<String, Outcome> outcomes(Class<?> testClass) {
        return OUTCOMES.getOrDefault(testClass, Map.of());
    }

    private static void record(ExtensionContext context, Outcome outcome) {
        OUTCOMES.computeIfAbsent(context.getRequiredTestClass(), testClass -> new ConcurrentHashMap<>())
                .put(context.getRequiredTestMethod().getName() + " " + context.getDisplayName(), outcome);
    }
}
