/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit.event;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;
import org.junit.runners.model.TestClass;

/**
 * Event fired Before running the rules enrichment.
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class RulesEnrichment extends BeforeTestLifecycleEvent {

    private TestClass testClassInstance;

    /**
     * @param testInstance      The test case instance being tested
     * @param testClassInstance The {@link TestClass} instance representing the test case
     * @param testMethod        The test method that is about to be executed
     * @param executor          A call back when the LifecycleMethod represented by this event should be invoked
     */
    public RulesEnrichment(Object testInstance, TestClass testClassInstance, Method testMethod,
        LifecycleMethodExecutor executor) {
        super(testInstance, testMethod, executor);
        this.testClassInstance = testClassInstance;
    }

    public TestClass getTestClassInstance() {
        return testClassInstance;
    }

}
