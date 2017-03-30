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
package org.jboss.arquillian.test.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;
import org.jboss.arquillian.test.spi.execution.TestExecutionDecider;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Verifies that the {@link EventTestRunnerAdaptor} creates and fires the proper events.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class EventTestRunnerAdaptorTestCase extends AbstractTestTestBase {

    private static final TestExecutionDecider NEGATIVE_EXECUTION_DECIDER = new TestExecutionDecider() {

        @Override
        public ExecutionDecision decide(Method testMethod) {
            return ExecutionDecision.dontExecute("Skipping execution of test method: " + testMethod.getName());
        }

        @Override
        public int precedence() {
            return 0;
        }
    };

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(TestContextHandler.class);
    }

    @Override
    protected void startContexts(Manager manager) {
        // this is a test of the Context activation, don't auto start.

    }

    @Test
    public void shouldSkipWhenUsingExecutionDecider() throws Exception {

        List<TestExecutionDecider> deciders = new ArrayList<TestExecutionDecider>();
        deciders.add(NEGATIVE_EXECUTION_DECIDER);

        ServiceLoader serviceLoder = Mockito.mock(ServiceLoader.class);
        Mockito.when(serviceLoder.all(TestExecutionDecider.class)).thenReturn(deciders);

        Manager manager = Mockito.spy(getManager());
        Mockito.when(manager.resolve(ServiceLoader.class)).thenReturn(serviceLoder);

        EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(manager);

        Class<?> testClass = getClass();
        Method testMethod = testClass.getMethod("shouldSkipWhenUsingExecutionDecider");
        Object testInstance = this;

        TestMethodExecutor testExecutor = Mockito.mock(TestMethodExecutor.class);
        Mockito.when(testExecutor.getInstance()).thenReturn(testInstance);
        Mockito.when(testExecutor.getMethod()).thenReturn(testMethod);

        // ApplicationContext is auto started, deactivate to be future proof
        manager.getContext(ApplicationContext.class).deactivate();

        verifyNoActiveContext(manager);

        adaptor.beforeSuite();
        assertEventFired(BeforeSuite.class, 1);
        assertEventFiredInContext(BeforeSuite.class, ApplicationContext.class);
        assertEventFiredInContext(BeforeSuite.class, SuiteContext.class);

        verifyNoActiveContext(manager);

        adaptor.beforeClass(testClass, LifecycleMethodExecutor.NO_OP);
        assertEventFired(BeforeClass.class, 1);
        assertEventFiredInContext(BeforeClass.class, ApplicationContext.class);
        assertEventFiredInContext(BeforeClass.class, SuiteContext.class);
        assertEventFiredInContext(BeforeClass.class, ClassContext.class);

        verifyNoActiveContext(manager);

        adaptor.before(testInstance, testMethod, LifecycleMethodExecutor.NO_OP);
        assertEventFired(Before.class, 0);
        assertEventNotFiredInContext(Before.class, ApplicationContext.class);
        assertEventNotFiredInContext(Before.class, SuiteContext.class);
        assertEventNotFiredInContext(Before.class, ClassContext.class);
        assertEventNotFiredInContext(Before.class, TestContext.class);

        verifyNoActiveContext(manager);

        adaptor.test(testExecutor);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Test.class, 0);
        assertEventNotFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, ApplicationContext.class);
        assertEventNotFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, SuiteContext.class);
        assertEventNotFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, ClassContext.class);
        assertEventNotFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, TestContext.class);

        verifyNoActiveContext(manager);

        adaptor.after(testInstance, testMethod, LifecycleMethodExecutor.NO_OP);
        assertEventFired(After.class, 0);
        assertEventNotFiredInContext(After.class, ApplicationContext.class);
        assertEventNotFiredInContext(After.class, SuiteContext.class);
        assertEventNotFiredInContext(After.class, ClassContext.class);
        assertEventNotFiredInContext(After.class, TestContext.class);

        verifyNoActiveContext(manager);

        adaptor.fireCustomLifecycle(
            new BeforeTestLifecycleEvent(testInstance, testMethod, LifecycleMethodExecutor.NO_OP));
        assertEventFired(BeforeTestLifecycleEvent.class, 0);
        assertEventNotFiredInContext(BeforeTestLifecycleEvent.class, ApplicationContext.class);
        assertEventNotFiredInContext(BeforeTestLifecycleEvent.class, SuiteContext.class);
        assertEventNotFiredInContext(BeforeTestLifecycleEvent.class, ClassContext.class);
        assertEventNotFiredInContext(BeforeTestLifecycleEvent.class, TestContext.class);

        verifyNoActiveContext(manager);

        adaptor.afterClass(testClass, LifecycleMethodExecutor.NO_OP);
        assertEventFired(AfterClass.class, 1);
        assertEventFiredInContext(AfterClass.class, ApplicationContext.class);
        assertEventFiredInContext(AfterClass.class, SuiteContext.class);
        assertEventFiredInContext(AfterClass.class, ClassContext.class);

        verifyNoActiveContext(manager);

        adaptor.afterSuite();
        assertEventFired(AfterSuite.class, 1);
        assertEventFiredInContext(AfterSuite.class, ApplicationContext.class);
        assertEventFiredInContext(AfterSuite.class, SuiteContext.class);

        verifyNoActiveContext(manager);
    }

    @Test
    public void shouldHandleLifeCycleEvents() throws Exception {
        Manager manager = getManager();
        manager.bind(ApplicationScoped.class, TestResult.class, TestResult.passed());
        EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(manager);

        Class<?> testClass = getClass();
        Method testMethod = testClass.getMethod("shouldHandleLifeCycleEvents");
        Object testInstance = this;

        TestMethodExecutor testExecutor = Mockito.mock(TestMethodExecutor.class);
        Mockito.when(testExecutor.getInstance()).thenReturn(testInstance);
        Mockito.when(testExecutor.getMethod()).thenReturn(testMethod);

        // ApplicationContext is auto started, deactivate to be future proof
        manager.getContext(ApplicationContext.class).deactivate();

        verifyNoActiveContext(manager);

        adaptor.beforeSuite();
        assertEventFired(BeforeSuite.class, 1);
        assertEventFiredInContext(BeforeSuite.class, ApplicationContext.class);
        assertEventFiredInContext(BeforeSuite.class, SuiteContext.class);

        verifyNoActiveContext(manager);

        adaptor.beforeClass(testClass, LifecycleMethodExecutor.NO_OP);
        assertEventFired(BeforeClass.class, 1);
        assertEventFiredInContext(BeforeClass.class, ApplicationContext.class);
        assertEventFiredInContext(BeforeClass.class, SuiteContext.class);
        assertEventFiredInContext(BeforeClass.class, ClassContext.class);

        verifyNoActiveContext(manager);

        adaptor.before(testInstance, testMethod, LifecycleMethodExecutor.NO_OP);
        assertEventFired(Before.class, 1);
        assertEventFiredInContext(Before.class, ApplicationContext.class);
        assertEventFiredInContext(Before.class, SuiteContext.class);
        assertEventFiredInContext(Before.class, ClassContext.class);
        assertEventFiredInContext(Before.class, TestContext.class);

        verifyNoActiveContext(manager);

        adaptor.test(testExecutor);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Test.class, 1);
        assertEventFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, ApplicationContext.class);
        assertEventFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, SuiteContext.class);
        assertEventFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, ClassContext.class);
        assertEventFiredInContext(org.jboss.arquillian.test.spi.event.suite.Test.class, TestContext.class);

        verifyNoActiveContext(manager);

        adaptor.after(testInstance, testMethod, LifecycleMethodExecutor.NO_OP);
        assertEventFired(After.class, 1);
        assertEventFiredInContext(After.class, ApplicationContext.class);
        assertEventFiredInContext(After.class, SuiteContext.class);
        assertEventFiredInContext(After.class, ClassContext.class);
        assertEventFiredInContext(After.class, TestContext.class);

        verifyNoActiveContext(manager);

        adaptor.afterClass(testClass, LifecycleMethodExecutor.NO_OP);
        assertEventFired(AfterClass.class, 1);
        assertEventFiredInContext(AfterClass.class, ApplicationContext.class);
        assertEventFiredInContext(AfterClass.class, SuiteContext.class);
        assertEventFiredInContext(AfterClass.class, ClassContext.class);

        verifyNoActiveContext(manager);

        adaptor.afterSuite();
        assertEventFired(AfterSuite.class, 1);
        assertEventFiredInContext(AfterSuite.class, ApplicationContext.class);
        assertEventFiredInContext(AfterSuite.class, SuiteContext.class);

        verifyNoActiveContext(manager);
    }

    private void verifyNoActiveContext(Manager manager) {
        verify(false, false, false, false, manager);
    }

    private void verify(boolean application, boolean suite, boolean clazz, boolean test, Manager manager) {
        Assert.assertEquals(
            "ApplicationContext should" + (!application ? " not" : "") + " be active",
            application, manager.getContext(ApplicationContext.class).isActive());
        Assert.assertEquals(
            "SuiteContext should" + (!suite ? " not" : "") + " be active",
            suite, manager.getContext(SuiteContext.class).isActive());
        Assert.assertEquals(
            "ClassContext should" + (!clazz ? " not" : "") + " be active",
            clazz, manager.getContext(ClassContext.class).isActive());
        Assert.assertEquals(
            "TestContext should" + (!test ? " not" : "") + " be active",
            test, manager.getContext(TestContext.class).isActive());
    }
}
