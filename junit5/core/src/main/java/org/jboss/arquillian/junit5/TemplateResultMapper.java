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
package org.jboss.arquillian.junit5;

import java.util.Map;

import org.jboss.arquillian.test.spi.TestResult;

/**
 * Maps the single aggregated {@link TestResult} of an in-container test template execution back onto the individual
 * template invocations reported on the client side.
 */
class TemplateResultMapper {

    /**
     * The unique ID segment JUnit Jupiter appends to a test template for each of its invocations.
     */
    private static final String INVOCATION_SEGMENT = "/[test-template-invocation:";

    private final TestResult aggregatedResult;

    private final String templateUniqueId;

    TemplateResultMapper(TestResult aggregatedResult, String templateUniqueId) {
        this.aggregatedResult = aggregatedResult;
        this.templateUniqueId = templateUniqueId;
    }

    TestResult resultFor(String uniqueId) {
        if (aggregatedResult.getStatus() != TestResult.Status.FAILED
            || !(aggregatedResult.getThrowable() instanceof IdentifiedTestException)) {
            return aggregatedResult;
        }
        final Map<String, Throwable> collectedExceptions =
            ((IdentifiedTestException) aggregatedResult.getThrowable()).getCollectedExceptions();

        final Throwable failure = collectedExceptions.get(uniqueId);
        if (failure != null) {
            return TestResult.failed(failure);
        }
        // The container reported no failure for this invocation. That only means the invocation passed if every
        // failure the container did report belongs to an invocation of this very template. Anything else - the
        // template itself being skipped in the container, or a container run whose invocations do not line up with
        // the client side - cannot be attributed, and reporting a pass would silently turn a red build green.
        if (collectedExceptions.keySet().stream().allMatch(this::isInvocationOfThisTemplate)) {
            return TestResult.passed();
        }
        return aggregatedResult;
    }

    private boolean isInvocationOfThisTemplate(String uniqueId) {
        return uniqueId.startsWith(templateUniqueId + INVOCATION_SEGMENT);
    }
}
