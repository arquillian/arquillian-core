/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.arquillian.test.spi.execution;

import java.lang.reflect.Method;

/**
 * Override the execution of the Before/Test/After phase of the
 * Test framework.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public interface TestExecutionDecider {

    static final TestExecutionDecider EXECUTE = new TestExecutionDecider() {

        @Override
        public ExecutionDecision decide(Method testMethod) {
            return ExecutionDecision.execute();
        }

        @Override
        public int precedence() {
            return 0;
        }
    };

    /**
     * This method will be called individually for each event(before/test/after), but
     * should return the same result for each to behave consistently.
     *
     * @param testMethod
     *     test method to resolve a test execution on. This is always the @Test method regardless of the current phase
     *     before or after.
     *
     * @return execution decision telling if a test method is going to be executed or not
     */
    ExecutionDecision decide(Method testMethod);

    /**
     * Higher the precedence is, sooner this decider will be treated.
     *
     * @return precedence of this decider
     */
    int precedence();
}
