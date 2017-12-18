/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.test.spi;

import java.lang.reflect.Method;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;

/**
 * TestRunnerAdaptor
 * <p>
 * Need to be Thread-safe
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface TestRunnerAdaptor {
    /**
     * Activate a new TestSuite.<br/>
     * This will trigger the BeforeSuite event.
     *
     * @throws Exception
     */
    void beforeSuite() throws Exception;

    /**
     * Deactivate the TestSuite.<br/>
     * This will trigger the AfterSuite event.
     *
     * @throws Exception
     */
    void afterSuite() throws Exception;

    /**
     * Activate a new TestClass.<br/>
     * This will trigger the BeforeClass event.
     *
     * @throws Exception
     */
    void beforeClass(Class<?> testClass, LifecycleMethodExecutor executor) throws Exception;

    /**
     * Deactivate the TestClass.<br/>
     * This will trigger the AfterClass event.
     *
     * @throws Exception
     */
    void afterClass(Class<?> testClass, LifecycleMethodExecutor executor) throws Exception;

    /**
     * Activate a new TestInstance.<br/>
     * This will trigger the Before event.
     *
     * @throws Exception
     */
    void before(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception;

    /**
     * Deactivate the TestInstance.<br/>
     * This will trigger the After event.
     *
     * @throws Exception
     */
    void after(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception;

    /**
     * Activate a TestMethod execution.<br/>
     * This will trigger the Test event.
     *
     * @throws Exception
     */
    TestResult test(TestMethodExecutor testMethodExecutor) throws Exception;

    /**
     * Fire any custom Test Lifecycle event.<br/>
     * <br/>
     * This can be used by a TestFramework to trigger e.g. additional Lifecycle
     * phases not described directly by the Test SPI.
     *
     * @param event
     *     Any event
     *
     * @throws Exception
     */
    <T extends TestLifecycleEvent> void fireCustomLifecycle(T event) throws Exception;

    /**
     * Shutdown Arquillian cleanly.
     */
    void shutdown();
}
