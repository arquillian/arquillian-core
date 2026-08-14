/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.test.spi;

import java.lang.reflect.Method;

/**
 * Listener SPI for the test suite lifecycle event package
 * ({@code org.jboss.arquillian.test.spi.event.suite}).
 *
 * <p>Register instances via {@link org.jboss.arquillian.core.spi.Manager#addListener(Class, Object)}.
 * Each method defaults to a no-op so implementors only override what they need.
 *
 * @author Arquillian
 */
public interface TestLifecycleListener {

    default void beforeSuite() throws Exception {
    }

    default void afterSuite() throws Exception {
    }

    default void beforeClass(TestClass testClass, LifecycleMethodExecutor executor) throws Exception {
    }

    default void afterClass(TestClass testClass, LifecycleMethodExecutor executor) throws Exception {
    }

    default void before(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception {
    }

    default void after(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception {
    }
}
