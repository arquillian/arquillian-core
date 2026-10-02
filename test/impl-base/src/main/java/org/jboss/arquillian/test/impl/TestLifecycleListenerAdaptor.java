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
package org.jboss.arquillian.test.impl;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.test.spi.TestLifecycleListener;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * Adaptor that bridges test suite lifecycle events to the {@link TestLifecycleListener} SPI.
 *
 * @author Arquillian
 */
public class TestLifecycleListenerAdaptor {

    @Inject
    private Instance<Manager> manager;

    public void onBeforeSuite(@Observes BeforeSuite event) throws Exception {
        for (TestLifecycleListener listener : manager.get().getListeners(TestLifecycleListener.class)) {
            listener.beforeSuite();
        }
    }

    public void onAfterSuite(@Observes AfterSuite event) throws Exception {
        for (TestLifecycleListener listener : manager.get().getListeners(TestLifecycleListener.class)) {
            listener.afterSuite();
        }
    }

    public void onBeforeClass(@Observes BeforeClass event) throws Exception {
        for (TestLifecycleListener listener : manager.get().getListeners(TestLifecycleListener.class)) {
            listener.beforeClass(event.getTestClass(), event.getExecutor());
        }
    }

    public void onAfterClass(@Observes AfterClass event) throws Exception {
        for (TestLifecycleListener listener : manager.get().getListeners(TestLifecycleListener.class)) {
            listener.afterClass(event.getTestClass(), event.getExecutor());
        }
    }

    public void onBefore(@Observes Before event) throws Exception {
        for (TestLifecycleListener listener : manager.get().getListeners(TestLifecycleListener.class)) {
            listener.before(event.getTestInstance(), event.getTestMethod(), event.getExecutor());
        }
    }

    public void onAfter(@Observes After event) throws Exception {
        for (TestLifecycleListener listener : manager.get().getListeners(TestLifecycleListener.class)) {
            listener.after(event.getTestInstance(), event.getTestMethod(), event.getExecutor());
        }
    }
}
