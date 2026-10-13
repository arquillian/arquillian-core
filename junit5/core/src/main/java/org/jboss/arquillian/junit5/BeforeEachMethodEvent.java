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

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;

/**
 * An event fired around the invocation of a single {@code @BeforeEach} method. Firing it activates the same contexts
 * as the {@link org.jboss.arquillian.test.spi.event.suite.Before} event and invokes the executor where the
 * lifecycle methods are to be executed, without firing the {@link org.jboss.arquillian.test.spi.event.suite.Before}
 * event itself again for every {@code @BeforeEach} method.
 *
 * @author Radoslav Husar
 */
class BeforeEachMethodEvent extends BeforeTestLifecycleEvent {

    /**
     * Creates a new event.
     *
     * @param testInstance The test case instance
     * @param testMethod   The test method
     * @param executor     A call back to invoke the {@code @BeforeEach} method
     */
    BeforeEachMethodEvent(final Object testInstance, final Method testMethod, final LifecycleMethodExecutor executor) {
        super(testInstance, testMethod, executor);
    }
}
