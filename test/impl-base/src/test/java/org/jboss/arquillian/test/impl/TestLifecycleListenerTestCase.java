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

import java.lang.reflect.Method;
import java.util.List;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.TestLifecycleListener;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies that {@link TestLifecycleListener} instances registered via
 * {@link Manager#addListener(Class, Object)} are notified for test suite lifecycle events.
 */
public class TestLifecycleListenerTestCase extends AbstractTestTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(TestLifecycleListenerAdaptor.class);
    }

    @Test
    public void shouldNotifyListenerOnBeforeAndAfterSuite() throws Exception {
        TrackingTestLifecycleListener listener = new TrackingTestLifecycleListener();
        getManager().addListener(TestLifecycleListener.class, listener);

        fire(new BeforeSuite());
        Assert.assertTrue("beforeSuite() should have been called", listener.beforeSuite);

        fire(new AfterSuite());
        Assert.assertTrue("afterSuite() should have been called", listener.afterSuite);
    }

    @Test
    public void shouldNotifyListenerOnBeforeAndAfterClass() throws Exception {
        TrackingTestLifecycleListener listener = new TrackingTestLifecycleListener();
        getManager().addListener(TestLifecycleListener.class, listener);

        Class<?> clazz = TestLifecycleListenerTestCase.class;
        fire(new BeforeClass(clazz, LifecycleMethodExecutor.NO_OP));
        Assert.assertTrue("beforeClass() should have been called", listener.beforeClass);
        Assert.assertSame("underlying class should be passed to beforeClass()",
            clazz, listener.testClass.getJavaClass());

        fire(new AfterClass(clazz, LifecycleMethodExecutor.NO_OP));
        Assert.assertTrue("afterClass() should have been called", listener.afterClass);
    }

    @Test
    public void shouldNotifyListenerOnBeforeAndAfterTest() throws Exception {
        TrackingTestLifecycleListener listener = new TrackingTestLifecycleListener();
        getManager().addListener(TestLifecycleListener.class, listener);

        Object testInstance = this;
        Method testMethod = TestLifecycleListenerTestCase.class
            .getMethod("shouldNotifyListenerOnBeforeAndAfterTest");

        fire(new Before(testInstance, testMethod, LifecycleMethodExecutor.NO_OP));
        Assert.assertTrue("before() should have been called", listener.before);
        Assert.assertSame("testInstance should be passed to before()", testInstance, listener.testInstance);
        Assert.assertSame("testMethod should be passed to before()", testMethod, listener.testMethod);

        fire(new After(testInstance, testMethod, LifecycleMethodExecutor.NO_OP));
        Assert.assertTrue("after() should have been called", listener.after);
    }

    private static class TrackingTestLifecycleListener implements TestLifecycleListener {
        boolean beforeSuite = false;
        boolean afterSuite = false;
        boolean beforeClass = false;
        boolean afterClass = false;
        boolean before = false;
        boolean after = false;
        TestClass testClass = null;
        Object testInstance = null;
        Method testMethod = null;

        @Override
        public void beforeSuite() throws Exception {
            beforeSuite = true;
        }

        @Override
        public void afterSuite() throws Exception {
            afterSuite = true;
        }

        @Override
        public void beforeClass(TestClass tc, LifecycleMethodExecutor executor) throws Exception {
            beforeClass = true;
            testClass = tc;
        }

        @Override
        public void afterClass(TestClass tc, LifecycleMethodExecutor executor) throws Exception {
            afterClass = true;
        }

        @Override
        public void before(Object instance, Method method, LifecycleMethodExecutor executor) throws Exception {
            before = true;
            testInstance = instance;
            testMethod = method;
        }

        @Override
        public void after(Object instance, Method method, LifecycleMethodExecutor executor) throws Exception {
            after = true;
        }
    }
}
