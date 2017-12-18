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
package org.jboss.arquillian.junit.extension;

import junit.framework.Assert;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.junit.State;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

// TODO: should have a complete 'integration' test case with Arquillian.class / InContainer vs Client
@RunWith(MockitoJUnitRunner.class)
public class UpdateTestResultBeforeAfterTestCase {

    @Mock
    private EventContext<AfterTestLifecycleEvent> event;

    @Test
    public void shouldPassResultIfNoExceptionCaughtAfterJunit() throws Exception {
        TestResult result = TestResult.failed(new NullPointerException());

        new UpdateTestResultBeforeAfter().update(event, result);
        Assert.assertEquals(Status.PASSED, result.getStatus());
        Assert.assertNull(result.getThrowable());
    }

    @Test
    public void shouldSkipResultIfExceptionCaughtAfterJunit() throws Exception {
        State.caughtExceptionAfterJunit(new AssumptionViolatedException("A"));
        TestResult result = TestResult.failed(new AssumptionViolatedException("A"));

        new UpdateTestResultBeforeAfter().update(event, result);
        State.caughtExceptionAfterJunit(null);

        Assert.assertEquals(Status.SKIPPED, result.getStatus());
        Assert.assertNotNull(result.getThrowable());
        Assert.assertTrue(result.getThrowable() instanceof AssumptionViolatedException);
    }

    @Test
    public void shouldFailResultIfExceptionCaughtAfterJunit() throws Exception {
        State.caughtExceptionAfterJunit(new AssertionError("A"));
        TestResult result = TestResult.failed(new NullPointerException("A"));

        new UpdateTestResultBeforeAfter().update(event, result);
        State.caughtExceptionAfterJunit(null);

        Assert.assertEquals(Status.FAILED, result.getStatus());
        Assert.assertNotNull(result.getThrowable());
        Assert.assertTrue(result.getThrowable() instanceof AssertionError);
    }
}
