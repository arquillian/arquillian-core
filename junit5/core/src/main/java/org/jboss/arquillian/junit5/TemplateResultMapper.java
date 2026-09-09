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

import org.jboss.arquillian.test.spi.TestResult;

class TemplateResultMapper {

    private final TestResult aggregatedResult;

    TemplateResultMapper(TestResult aggregatedResult) {
        this.aggregatedResult = aggregatedResult;
    }

    TestResult resultFor(String uniqueId) {
        if (aggregatedResult == null
                || aggregatedResult.getStatus() == TestResult.Status.PASSED) {
            return aggregatedResult;
        }

        if (aggregatedResult.getStatus() == TestResult.Status.FAILED
            && aggregatedResult.getThrowable() instanceof IdentifiedTestException) {
            IdentifiedTestException identified = (IdentifiedTestException) aggregatedResult.getThrowable();

            Throwable failure = identified.getCollectedExceptions().get(uniqueId);
            return failure == null
                    ? TestResult.passed()
                    : TestResult.failed(failure);
        }
        return aggregatedResult;
    }
}
