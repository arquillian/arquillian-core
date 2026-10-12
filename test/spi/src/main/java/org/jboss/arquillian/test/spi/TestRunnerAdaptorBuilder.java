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

import java.util.function.Supplier;

import org.jboss.arquillian.core.spi.ManagerBuilder;

/**
 * DeployableTestBuilder
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 */
public class TestRunnerAdaptorBuilder {
    private static final String DEFAULT_EXTENSION_CLASS =
        "org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader";
    private static final String TEST_RUNNER_IMPL_CLASS = "org.jboss.arquillian.test.impl.EventTestRunnerAdaptor";
    private static volatile Supplier<TestRunnerAdaptor> testAdaptorFactory;

    private TestRunnerAdaptorBuilder() {
    }

    /**
     * Overrides what {@link #build()} returns with a single fixed adaptor.
     *
     * <p><b>Note for callers that build more than one adaptor per run</b>, such as the JUnit Jupiter extension in
     * class manager scope: every manager then shares this one adaptor, so the run no longer reflects how the
     * production code behaves. Use {@link #setFactory(Supplier)} instead to hand out one adaptor per manager.</p>
     *
     * @param adaptor the adaptor every {@code build()} call returns, or {@code null} to restore normal behaviour
     */
    // Hidden from public, use reflection to invoke. Used for Test
    public static void set(TestRunnerAdaptor adaptor) {
        testAdaptorFactory = adaptor == null ? null : () -> adaptor;
    }

    /**
     * Overrides what {@link #build()} returns with a factory consulted on every call, so that each Arquillian
     * manager gets its own adaptor.
     *
     * <p>Deliberately not an overload of {@link #set(TestRunnerAdaptor)}: that would make the existing
     * {@code set(null)} reset call ambiguous, and so stop it compiling.</p>
     *
     * @param adaptorFactory produces an adaptor per {@code build()} call, or {@code null} to restore normal
     *                       behaviour
     */
    // Hidden from public, use reflection to invoke. Used for Test
    public static void setFactory(Supplier<TestRunnerAdaptor> adaptorFactory) {
        testAdaptorFactory = adaptorFactory;
    }

    public static TestRunnerAdaptor build() {
        Supplier<TestRunnerAdaptor> factory = testAdaptorFactory;
        if (factory != null) {
            return factory.get();
        }

        ManagerBuilder builder = ManagerBuilder.from()
            .extension(SecurityActions.loadClass(DEFAULT_EXTENSION_CLASS));

        return SecurityActions.newInstance(
            TEST_RUNNER_IMPL_CLASS,
            new Class<?>[] {ManagerBuilder.class},
            new Object[] {builder},
            TestRunnerAdaptor.class);
    }
}
