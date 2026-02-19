/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;

/**
 * An event triggered before each test is invoked.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class BeforeTestExecutionEvent extends TestLifecycleEvent {
    /**
     * Creates a new event.
     *
     * @param testInstance The test case instance
     * @param testMethod   The test method
     *
     * @throws IllegalArgumentException if testInstance is null
     * @throws IllegalArgumentException if testMethod is null
     */
    BeforeTestExecutionEvent(final Object testInstance, final Method testMethod) {
        super(testInstance, testMethod);
    }
}
