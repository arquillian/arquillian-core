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
import org.jboss.arquillian.test.spi.TestEnrichmentListener;
import org.jboss.arquillian.test.spi.event.enrichment.AfterEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.BeforeEnrichment;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies that {@link TestEnrichmentListener} instances registered via
 * {@link Manager#addListener(Class, Object)} are notified for enrichment events.
 */
public class TestEnrichmentListenerTestCase extends AbstractTestTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(TestEnrichmentListenerAdaptor.class);
    }

    @Test
    public void shouldNotifyListenerOnBeforeAndAfterEnrichment() throws Exception {
        TrackingTestEnrichmentListener listener = new TrackingTestEnrichmentListener();
        getManager().addListener(TestEnrichmentListener.class, listener);

        Object testInstance = this;
        Method testMethod = TestEnrichmentListenerTestCase.class
            .getMethod("shouldNotifyListenerOnBeforeAndAfterEnrichment");

        fire(new BeforeEnrichment(testInstance, testMethod));
        Assert.assertTrue("beforeEnrichment() should have been called", listener.beforeEnrichment);
        Assert.assertSame("testInstance should be passed to beforeEnrichment()", testInstance, listener.instance);
        Assert.assertSame("testMethod should be passed to beforeEnrichment()", testMethod, listener.method);

        fire(new AfterEnrichment(testInstance, testMethod));
        Assert.assertTrue("afterEnrichment() should have been called", listener.afterEnrichment);
    }

    private static class TrackingTestEnrichmentListener implements TestEnrichmentListener {
        boolean beforeEnrichment = false;
        boolean afterEnrichment = false;
        Object instance = null;
        Method method = null;

        @Override
        public void beforeEnrichment(Object testInstance, Method testMethod) throws Exception {
            beforeEnrichment = true;
            instance = testInstance;
            method = testMethod;
        }

        @Override
        public void afterEnrichment(Object testInstance, Method testMethod) throws Exception {
            afterEnrichment = true;
        }
    }
}
