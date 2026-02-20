/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
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

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;

/**
 * An event which is fired and allows an observer to set the {@link MethodParameters} on a producer for later usage
 * in {@link org.jboss.arquillian.test.spi.event.suite.Before} events.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MethodParameterProducerEvent extends TestLifecycleEvent {

    private final MethodParameters testParameterHolder;

    MethodParameterProducerEvent(final Object testInstance, final Method testMethod, final MethodParameters testParameterHolder) {
        super(testInstance, testMethod, LifecycleMethodExecutor.NO_OP);
        this.testParameterHolder = testParameterHolder;
    }

    MethodParameters getTestParameterHolder() {
        return testParameterHolder;
    }
}
